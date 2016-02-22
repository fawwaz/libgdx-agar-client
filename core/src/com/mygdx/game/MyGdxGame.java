package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.sprites.Cell;
import com.mygdx.game.sprites.Food;
import com.mygdx.game.sprites.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class MyGdxGame implements Screen {
	Agar game;

	//Related to game
	String nama;
	Player the_player;
	Vector2 the_player_target;
	Array<Player> the_players;
	Array<Food> foods;

	// Related to draw
	OrthographicCamera camera;
	SpriteBatch batch;
	Texture img;
	Integer screenwidth, screenheight;
	int gamewidth, gameheight;
	private Socket socket;
	ShapeRenderer shapeRenderer;
	BitmapFont font;

	// State game
	boolean gameStart = false;
	boolean disconnected = false;
	boolean died = false;
	boolean kicked = false;
	String reason;

	// Standard Configuration
	String backgroundColor = "#f2fbff";
	String lineColor = "#000000";

	// Some constant
	double spin;
	double enemyspin;

	public MyGdxGame(final Agar gam, String nama) {
		this.game = gam;
		this.nama = nama;

		screenwidth = Gdx.graphics.getWidth();
		screenheight = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenwidth, screenheight);

		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");


		// Start awal configure by default target selalu 0,0 sebelum disentuh
		the_player_target = new Vector2();
		the_player_target.set(0,0);

		spin = -Math.PI;
		enemyspin = -Math.PI;

		font = new BitmapFont();
		connectSocket();
		configSocketEvents();
		startGame();
	}

	private void connectSocket(){
		try{
			IO.Options options = new IO.Options();
			options.forceNew = true;
			options.query = "type=player";
			socket = IO.socket("http://localhost:3000",options);
//			socket = IO.socket("http://myagar.herokuapp.com:80",options);
//			socket = IO.socket("http://192.168.43.57:3000",options);
			socket.connect();
		}catch(Exception e){
			System.out.println(e);
		}
	}

	private void startGame() {
		// do something above..
		try{
			JSONObject obj = new JSONObject();
			obj.put("name",nama);
			socket.emit("respawn", obj);
		}catch(JSONException e){
			Gdx.app.log("SOCKETIO", "Failed to send respawn object");
		}

	}

	@Override
	public void render(float delta) {
		// Game state
		handleInput();
		handleGameState();



		// Cara bikin lingkaran :
		/*
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.circle(100,100,20);
		shapeRenderer.end();
		*/

	}

	private void configSocketEvents(){
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SOCKETIO","connected to server");
			}
		}).on("welcome", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject player = (JSONObject) args[0];
				try {
					// Will be used for handling cells and setting the target
					JSONObject target_player 	= (JSONObject) player.getJSONObject("target");
					JSONArray _cells			= (JSONArray) player.getJSONArray("cells");

					// Handle x, id and y
					String _id 	= player.getString("id");
					Double _x 	= player.getDouble("x");
					Double _y 	= player.getDouble("y");

					// Handle Cells and assign it to player
					Array<Cell> _player_cells = new Array<Cell>();
					for (int i=0; i<_cells.length(); i++){
						JSONObject _cell = (JSONObject) _cells.get(i);
						Double c_mass 	= _cell.getDouble("mass");
						double c_x 		= _cell.getDouble("x");
						double c_y		= _cell.getDouble("y");
						Double c_radius 	= _cell.getDouble("radius");
						//Integer c_speed	= _cell.getInt("speed");
						Cell c = new Cell(c_mass,c_x,c_y,c_radius,0);
						_player_cells.add(c);
					}

					// Handle mass, hue, type and lastheartbeat
					Double _massTotal 	= player.getDouble("massTotal");
					Integer _hue		= player.getInt("hue");
					String _type		= player.getString("type");
					Long _lastheartbeat = player.getLong("lastHeartbeat");


					// Set everything to local
					the_player = new Player(_id,nama,_x,_y);
					the_player.setScreenWidth(Gdx.graphics.getWidth());
					the_player.setScreenHeight(Gdx.graphics.getHeight());

					the_player.setTarget_x(target_player.getInt("x"));
					the_player.setTarget_y(target_player.getInt("y"));

					the_player.setCells(_player_cells);

					the_player.setMassTotal(_massTotal);
					the_player.setHue(_hue);
					the_player.setLastHeartbeat(_lastheartbeat);

					// Telling the server that we've received the object
					player.put("name",nama);
					player.put("screenWidth", Gdx.graphics.getWidth());
					player.put("screenHeight", Gdx.graphics.getHeight());
					JSONObject __target = new JSONObject();
					__target.put("x",the_player_target.x);
					__target.put("y",the_player_target.y);
					player.put("target",__target);
					socket.emit("gotit", player);

					/*
					Float massTotal = player.getDouble("massTotal");
					Integer y = player.getInt("hue");
					Long lastHeartbeat = player.getLong("lastHeartbeat");

					Integer target_x = target_player.getDouble("x");
					Integer target_y = target_player.getDouble("y");
					Vector2 target = new Vector2(target_x, target_y);

					Integer screenwidth = player.getInt("screenWidth");
					Integer screenheight = player.getInt("screenHeight");
					*/
					gameStart = true;
					Gdx.app.log("SOCKETIO", "Informasi player diapatkan");
				} catch (Exception e) {
					Gdx.app.log("SOCKETIO", "Failed to get json");
					Gdx.app.log("SOCKETIO", e.getMessage());
					e.printStackTrace();
				}
			}
		}).on("pong", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				long unixTime = System.currentTimeMillis() / 1000L;
				Gdx.app.log("SOCKETIO","unixtimestamp : "+ unixTime);
			}
		}).on("connect_failed", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SOCKETIO", "Failed to connect, closing socket");
				socket.close();
			}
		}).on("disconnect", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SOCKETIO", "Disconnected from server, closing socket");
				socket.close();
			}
		}).on("gameSetup", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					gamewidth = data.getInt("gameWidth");
					gameheight = data.getInt("gameHeight");
					//resize()
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to setup the game");
				}
			}
		}).on("playerDied", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String name = ((data.getString("name").length()) < 1) ? "An unamed cell" : data.getString("name");
					Gdx.app.log("SOCKETIO", "Player "+name+ " was eaten");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get died player");
				}
			}
		}).on("playerDisconnect", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String name = ((data.getString("name").length()) < 1) ? "An unamed cell" : data.getString("name");
					Gdx.app.log("SOCKETIO", "Player "+name+ " was disconnected");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get disconnected player");
				}
			}
		}).on("playerJoin", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String name = ((data.getString("name").length()) < 1) ? "An unamed cell" : data.getString("name");
					Gdx.app.log("SOCKETIO", "Player "+name+ " joined");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get recently joined player");
				}
			}
		}).on("leaderboard", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				// TODO bikin listing
				JSONObject data = (JSONObject) args[0];
				try {
					//String name = data.getString("name").length() < 1 ? 'An unamed cell' : data.getString("name");
					Gdx.app.log("SOCKETIO", "Sharusnya handle leaderboard");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get leaderboard");
				}
			}
		}).on("serverMSG", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Gdx.app.log("SOCKETIO", "Sharusnya handle server Message");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get server message");
				}
			}
		}).on("serverSendPlayerChat", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Gdx.app.log("SOCKETIO", "Sharusnya handle server chat");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get server chat");
				}
			}
		}).on("serverTellPlayerMove", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
//				JSONObject data = (JSONObject) args[0];
				JSONArray userdata = (JSONArray) args[0];
				JSONArray datafoods = (JSONArray) args[1];
				JSONArray datamasses = (JSONArray) args[2];
				JSONArray dataviruses = (JSONArray) args[3];
				/*
				System.out.println("user data : ");
				System.out.println(userdata);
				System.out.println("food data :");
				System.out.println(datafoods);
				System.out.println("datamass :");
				System.out.println(datamasses);
				System.out.println("datavirus :");
				System.out.println(dataviruses);
				*/
				try {
					// Handle player
					Double playerdata_x = null,playerdata_y = null;
					int playerdata_hue = 0;
					double playerdata_mass = 0;
					Array<Cell> playerdata_cells = new Array<Cell>();

					for (int i=0; i<userdata.length(); i++){
						JSONObject udata = (JSONObject) userdata.get(i);
						try{
							udata.get("id");
						}catch(JSONException jsonexcept){

							// Justru ketik agak bisa (id= undefined) ktia handle
							playerdata_hue 	= udata.getInt("hue");
							playerdata_mass = udata.getDouble("massTotal");
							playerdata_x 	= udata.getDouble("x");
							playerdata_y 	= udata.getDouble("y");


							JSONArray _the_cells = udata.getJSONArray("cells");
							// try consider to make it as function retrieving jsonarray as input and outputing "clean" version of array
							for(int j=0; j<_the_cells.length(); j++) {
								JSONObject _the_cell 	= (JSONObject) _the_cells.get(j);
								Double _tc_mass 		= _the_cell.getDouble("mass");
								Double _tc_x 			= _the_cell.getDouble("x");
								Double _tc_y 			= _the_cell.getDouble("y");
								Double _tc_radius 		= _the_cell.getDouble("radius");
								Integer _tc_speed 		= _the_cell.getInt("speed");

								Cell _tc_ = new Cell(_tc_mass, _tc_x, _tc_y, _tc_radius, _tc_speed);
								playerdata_cells.add(_tc_);
							}

							// Sejenis break, biar langsung selesai
							i = userdata.length();
						}
					}

					// Set player in client :

					try{
						float xoffset = (float) (the_player.getX() - playerdata_x);
						the_player.setXoffset(xoffset);
					}catch(Exception e){
						// Artinya x offset is NAN
						the_player.setXoffset(0);
					}

					try{
						float yoffset = (float) (the_player.getY() - playerdata_y);
						the_player.setXoffset(yoffset);
					}catch(Exception e){
						// Dengan kata lain  yoffset isNAN
						the_player.setXoffset(0);
					}

					// Set everything for player
					the_player.setHue(playerdata_hue);
					the_player.setMassTotal(playerdata_mass);
					the_player.setX(playerdata_x);
					the_player.setY(playerdata_y);
					the_player.setCells(playerdata_cells);


					// Part 2 : Handle food dulu
					Array<Food> _foods = new Array<Food>();
					for (int i=0; i<datafoods.length(); i++){
						JSONObject datafood = (JSONObject) datafoods.getJSONObject(i);
						try{
							String f_id 	= datafood.getString("id");
							double f_x 		= datafood.getDouble("x");
							double f_y 		= datafood.getDouble("y");
							Double f_mass	= datafood.getDouble("mass");
							Double f_radius = datafood.getDouble("radius");
							Integer f_hue 	= datafood.getInt("hue");
							Food f = new Food(f_id,f_x,f_y,f_radius,f_mass,f_hue);
							_foods.add(f);
						}catch(JSONException jsonexception){
							Gdx.app.log("ERROR","Uncaught exception when getting food data..");
						}
					}
					foods = _foods;

					// Part 3 : Handle player lain
					Array<Player> _players = new Array<Player>();
					System.out.println(userdata);
					for (int i=0; i<userdata.length(); i++){
						JSONObject datauser = (JSONObject) userdata.get(i);

						try{
							//JSONObject target_datauser = (JSONObject) datauser.getJSONObject("target");
							JSONArray cells_datauser = (JSONArray) datauser.getJSONArray("cells");


							Player p			= new Player();
							double u_x 			= datauser.getDouble("x");
							double u_y			= datauser.getDouble("y");
							Double _massTotal	= datauser.getDouble("massTotal");
							Integer _hue 		= datauser.getInt("hue");
							try{
								String u_id 		= datauser.getString("id");
								String u_name		= datauser.getString("name");
								p.setId(u_id);
								p.setName(u_name);
							}catch(JSONException jsexcept){
								Gdx.app.log("ERROR","It doesn't have id..");
							}
							p.setMassTotal(_massTotal);
							p.setX(u_x);
							p.setY(u_y);
							p.setHue(_hue);

							// Assume that we don't need every user properties, we just get several and if we need it, we can add it later

							/*
							Double _massTotal		= datauser.getDouble("massTotal");
							Integer _hue 			= datauser.getInt("hue");
							Long _lastHeartbeat 	= datauser.getLong("lastHearbeat");
							Integer _target_x		= target_datauser.getInt("x");
							Integer _target_y 		= target_datauser.getInt("y");
							Integer _screenWidth	= datauser.getInt("screenWidth");
							Integer _screenHeight	= datauser.getInt("screenHeight");
							Long _lastSplit			= datauser.getLong("lastSplit"); // kalau belum pernah split mungkin error karena tidak ada atribut ini.coba handle make finally...
							double __xoffset			= datauser.getDouble("xoffset");
							double __yoffset			= datauser.getDouble("yoffset");
							float _xoffset 			= (float) __xoffset;
							float _yoffset			= (float) __yoffset;
							*/

							// Handle array cells disini : IMPORTANT
							Array<Cell> _cells = new Array<Cell>();
							for(int j=0; j<cells_datauser.length(); j++){
								JSONObject cell_datauser = (JSONObject) cells_datauser.get(j);
								Double c_mass 			 = cell_datauser.getDouble("mass");
								double c_x 				 = cell_datauser.getDouble("x");
								double c_y 				 = cell_datauser.getDouble("y");
								double c_radius 		 = cell_datauser.getDouble("radius");
								Integer c_speed 		 = cell_datauser.getInt("speed");
								Cell _c = new Cell(c_mass,c_x,c_y,c_radius,c_speed);
								_cells.add(_c);
							}
							p.setCells(_cells);

							_players.add(p);
						}catch(JSONException jsonexception){
							// Berarti Id emang gak ada .. artinya cells.
							Gdx.app.log("ERROR","Uncaught exception when getting user data..");
							jsonexception.printStackTrace();
						}
					}
					the_players = _players;
					Gdx.app.log("SOCKETIO", "Sharusnya handle server tell player move");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get server server tell player move");
				}
			}
		}).on("RIP", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Gdx.app.log("SOCKETIO", "Sharusnya handle RIP");
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get RIP message");
				}
			}
		}).on("kick", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Gdx.app.log("SOCKETIO", "Sharusnya handle kick");
				} catch (Exception e) {
					Gdx.app.log("SOCKETIO", "Failed to get kick message");
				}
			}
		}).on("virusSplit", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					Gdx.app.log("SOCKETIO", "Sharusnya handle virusSplit");
				} catch (Exception e) {
					Gdx.app.log("SOCKETIO", "Failed to get virusSplit message");
				}
			}
		});
	}

	@Override
	public void dispose() {

	}

	@Override
	public void show() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	private void drawFullMessage(String message){
		Gdx.gl.glClearColor(0.4f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.font.draw(game.batch, message, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		game.batch.end();
	}

	private void handleGameState() {
		if(died){
			//game.setScreen(new DiedScreen(game,"You died because some reason"));
			drawFullMessage("You died because some reason");
		}else if(!disconnected){
			if(gameStart){
				//Gdx.app.log("SOCKETIO","Harusnya ngehandle start disini");
				Gdx.gl.glClearColor(1, 1, 1, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

				camera.update();

				game.batch.begin();
				//game.batch.draw(img, 0, 0);
				// Harusnya convert dar hsv ke RGB color versi kelas badlogic (RGB alpha)
				game.font.draw(game.batch, "Hi, " + nama + " !", Gdx.graphics.getHeight() / 2, Gdx.graphics.getWidth() / 2);
				game.batch.end();
				float[] rgb = hsvToRgb(the_player.getHue(),100,100);
				shapeRenderer.setColor(rgb[0],rgb[1],rgb[2],100);
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 40);
				shapeRenderer.end();



				drawfoods();

				// draw everything first

				// emiting to server that we are moving
				JSONObject target_obj = new JSONObject();
				try {
					target_obj.put("x",the_player_target.x);
					target_obj.put("y",the_player_target.y);
					socket.emit("0", target_obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				drawFullMessage("Game over !");
			}
		}else{
			if(kicked){
				if(reason.equals("")){
					drawFullMessage("You were kicked !");
				}else{
					drawFullMessage("You were kicked with some reason, please change this reason");
					//game.setScreen(new DiedScreen(game,"You were kicked with some reason, please change this reason"));
				}
			}else{
				drawFullMessage("Disconnected !");
			}
		}
	}



	private void handleInput() {
		if(Gdx.input.isTouched()){
			Vector3 touchpos = new Vector3();
			touchpos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchpos);
			the_player_target.set(touchpos.x-Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2-touchpos.y);
		}
	}

	private void drawfoods() {
		if(foods != null){
			Iterator<Food> iterator_food = foods.iterator();
			while(iterator_food.hasNext()){
				Food food = iterator_food.next();
				drawFood(food);
			}
		}
	}

	private void drawFood(Food food){
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.circle((int) (food.getX() - the_player.getX() + Gdx.graphics.getWidth()/2) ,(int) (the_player.getY() - food.getY() + Gdx.graphics.getHeight() /2 ) , food.getRadius().intValue());
		shapeRenderer.end();
	}

	private drawPlayers(Array<OrderMass> order){
		Vector2 start = new Vector2();
		start.x = the_player.getX() - (Gdx.graphics.getWidth()/2);
		start.y = the_player.getY() - (Gdx.graphics.getHeight()/2);

		for (int z=0; z<order.size; z++){
			Player currentplayer = the_players.get(order.get(z).nCell);
			Cell currentcell = the_players.get(order.get(z).nCell).cells.get(order.get(z).nDiv);

			double _x =0,_y=0;
			int points = (int) (30 + Math.floor(currentcell.mass / 5));
			double increase = Math.PI * 2 / points;

			int[] xstore = new int[points], ystore = new int[points];

			spin +=0;

			Vector2 circle = new Vector2();
			circle.x = (float) (currentcell.x - start.x);
			circle.y = (float) (currentcell.y - start.y);

			for (int i=0; i<points; i++){
				_x = currentcell.radius * Math.cos(spin) + circle.x;
				_y = currentcell.radius * Math.sin(spin) + circle.y;

				if(currentplayer.id == null){
					_x = valueinRange(-currentplayer.x + Gdx.graphics.getWidth() /2,gamewidth-currentplayer.x + Gdx.graphics.getWidth()/2, _x );
					_y = valueinRange(-currentplayer.y + Gdx.graphics.getHeight() / 2, gameheight - currentplayer.y + Gdx.graphics.getHeight() / 2, _y);
				}else{
					_x = valueinRange(-currentcell.x - the_player.x + Gdx.graphics.getWidth() /2 + currentcell.radius / 3 , gamewidth - currentcell.x + gamewidth - the_player.x + Gdx.graphics.getWidth() / 2 - currentcell.radius / 3,_x);
					_y = valueinRange(-currentcell.y - the_player.y + Gdx.graphics.getHeight() /2 + currentcell.radius/ 3 , gameheight - currentcell.y + gameheight - the_player.y + Gdx.graphics.getHeight()/2 - currentcell.radius /3 , _y);
				}
				spin+=increase;
				xstore[i] = (int) _x;
				ystore[i] = (int) _y;
			}

		}
	}

	private class OrderMass{
		public Integer nCell;
		public Integer nDiv;
		public Double mass;
	}

	private int randomRange(int min, int max){
		int range = max - min +1;
		return ((int) Math.random() * range) + min;
	}

	private int valueinRange(double min, double max, double value){
		return Math.min(max,Math.max(min,value));
	}

	/**
	 * @param H
	 *            0-360
	 * @param S
	 *            0-100
	 * @param V
	 *            0-100
	 * @return color in hex string
	 */
	public static float[] hsvToRgb(float H, float S, float V) {

		float R, G, B;

		H /= 360f;
		S /= 100f;
		V /= 100f;

		if (S == 0)
		{
			R = V * 255;
			G = V * 255;
			B = V * 255;
		} else {
			float var_h = H * 6;
			if (var_h == 6)
				var_h = 0; // H must be < 1
			int var_i = (int) Math.floor((double) var_h); // Or ... var_i =
			// floor( var_h )
			float var_1 = V * (1 - S);
			float var_2 = V * (1 - S * (var_h - var_i));
			float var_3 = V * (1 - S * (1 - (var_h - var_i)));

			float var_r;
			float var_g;
			float var_b;
			if (var_i == 0) {
				var_r = V;
				var_g = var_3;
				var_b = var_1;
			} else if (var_i == 1) {
				var_r = var_2;
				var_g = V;
				var_b = var_1;
			} else if (var_i == 2) {
				var_r = var_1;
				var_g = V;
				var_b = var_3;
			} else if (var_i == 3) {
				var_r = var_1;
				var_g = var_2;
				var_b = V;
			} else if (var_i == 4) {
				var_r = var_3;
				var_g = var_1;
				var_b = V;
			} else {
				var_r = V;
				var_g = var_1;
				var_b = var_2;
			}

			/*
			R = var_r * 255; // RGB results from 0 to 255
			G = var_g * 255;
			B = var_b * 255;
			*/
			R = var_r;
			G = var_g;
			B = var_b;
		}
		/*

		String rs = Integer.toHexString((int) (R));
		String gs = Integer.toHexString((int) (G));
		String bs = Integer.toHexString((int) (B));

		if (rs.length() == 1)
			rs = "0" + rs;
		if (gs.length() == 1)
			gs = "0" + gs;
		if (bs.length() == 1)
			bs = "0" + bs;
		int _1 = (int) R * 256;
		int _2 = (int) G * 256;
		int _3 = (int) B * 256;
		*/
		float[] retval = new float[3];
		retval[0] = R;
		retval[1] = G;
		retval[2] = B;
		return retval;
		//return "#" + R + "\n" + G + "\n" + B;
	}

}
