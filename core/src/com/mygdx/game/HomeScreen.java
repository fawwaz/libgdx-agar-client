package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Created by Asus on 15/02/2016.
 */
public class HomeScreen implements Screen,Input.TextInputListener {
    final Agar game;
    String text;

    OrthographicCamera camera;
    Integer screenwidth;
    Integer screenheight;

    public HomeScreen(final Agar gam){
        game = gam;

        screenwidth = Gdx.graphics.getWidth();
        screenheight = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false,screenwidth,screenheight);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.end();

        // Get input
        if(Gdx.input.justTouched()){
            Gdx.input.getTextInput(this, "Masukan nama", "Yup, nama kamu !", "");
        }

        if(this.text!=null){
            game.setScreen(new MyGdxGame(game,this.text));
            dispose();
        }

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

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void input(String text) {
        this.text = text;
    }

    @Override
    public void canceled() {
        this.text = "";
    }
}
