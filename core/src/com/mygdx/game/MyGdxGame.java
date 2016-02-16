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
import com.mygdx.game.sprites.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class MyGdxGame implements Screen {
	Agar game;

	//Related to game
	String nama;
	Player the_player;
	Vector2 the_player_target;
	HashMap<String,Player> players;

	// Related to draw
	OrthographicCamera camera;
	SpriteBatch batch;
	Texture img;
	Integer screenwidth, screenheight;
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
				Gdx.app.log("SOCKETIO","Failed to connect, closing socket");
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
					screenwidth = data.getInt("gameWidth");
					screenheight = data.getInt("gameHeight");
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
				JSONArray data = (JSONArray) args[0];
				JSONArray datafood = (JSONArray) args[1];
				JSONArray datamass = (JSONArray) args[2];
				JSONArray datavirus = (JSONArray) args[3];
				System.out.println("user data : ");
				System.out.println(data);
				System.out.println("food data :");
				System.out.println(datafood);
				System.out.println("datamass :");
				System.out.println(datamass);
				System.out.println("datavirus :");
				System.out.println(datavirus);
				try {
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
				game.font.draw(game.batch, "Hi, " + nama + " !", Gdx.graphics.getHeight() / 2, Gdx.graphics.getWidth() / 2);
				game.batch.end();
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
			the_player_target.set(touchpos.x-Gdx.graphics.getWidth()/2,touchpos.y-Gdx.graphics.getHeight()/2);
		}
	}
}
