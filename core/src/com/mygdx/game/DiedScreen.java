package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Created by Asus on 15/02/2016.
 */
public class DiedScreen implements Screen{
    String message;
    final Agar game;
    Integer screenwidth,screenheight;
    OrthographicCamera camera;

    public DiedScreen(final Agar gam,String text){
        game = gam;
        this.message = text;

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
}
