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
import com.mygdx.game.sprites.Player;

import org.json.JSONObject;
import java.util.HashMap;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class MyGdxGame implements Screen {
	Agar game;

	OrthographicCamera camera;

	String nama;
	Player player;
	HashMap<String,Player> players;
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
		camera.setToOrtho(false,screenwidth,screenheight);

		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		font = new BitmapFont();
		connectSocket();
		configSocketEvents();
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

	@Override
	public void render(float delta) {
		// Game state
		if(died){
			//game.setScreen(new DiedScreen(game,"You died because some reason"));
			drawFullMessage("You died because some reason");
		}else if(!disconnected){
			if(gameStart){
				Gdx.app.log("SOCKETIO","Harusnya ngehandle start disini");
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
					JSONObject target_player = (JSONObject) player.getJSONObject("target");
					gameStart = true;
					/*
					String id = player.getString("id");
					Float x = player.getDouble("x");
					Float y = player.getDouble("y");
					Float massTotal = player.getDouble("massTotal");
					Integer y = player.getInt("hue");
					Long lastHeartbeat = player.getLong("lastHeartbeat");

					Integer target_x = target_player.getDouble("x");
					Integer target_y = target_player.getDouble("y");
					Vector2 target = new Vector2(target_x, target_y);

					Integer screenwidth = player.getInt("screenWidth");
					Integer screenheight = player.getInt("screenHeight");
					*/
					Gdx.app.log("SOCKETIO", "Informasi player diapatkan");
				} catch (Exception e) {
					Gdx.app.log("SOCKETIO", "Failed to get json");
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
				JSONObject data = (JSONObject) args[0];
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
				}catch(Exception e){
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
}
