package com.mygdx.game.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Asus on 15/02/2016.
 */
public class Player extends Sprite{
    Vector2 previousposition;
    public Array<Cell> cells;
    public String id;
    public String name;
    public Float x;
    public Float y;
    public Long lastHeartbeat;

    public Player(){
        previousposition = new Vector2(getX(),getY());
    }

    public boolean hasmoved(){
        if(previousposition.x != getX() || previousposition.y != getY()){
            previousposition.x = getX();
            previousposition.y = getX();
            return true;
        }
        return false;
    }
}
