package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.sprites.Player;

import org.json.JSONObject;
import java.util.HashMap;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class MyGdxGame extends ApplicationAdapter {
	Player player;
	HashMap<String,Player> players;
	SpriteBatch batch;
	Texture img;
	Integer gamewidth,gameheight;
	private Socket socket;
	ShapeRenderer shapeRenderer;
	
	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
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
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.circle(100,100,20);
		shapeRenderer.end();

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
				}catch(Exception e){
					Gdx.app.log("SOCKETIO", "Failed to get virusSplit message");
				}
			}
		});
	}
}
