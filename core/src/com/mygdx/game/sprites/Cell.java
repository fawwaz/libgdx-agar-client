package com.mygdx.game.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Asus on 15/02/2016.
 */
public class Cell extends Sprite{
    Vector2 previousposition;
    public Float mass;
    public Float x;
    public Float y;
    public Float radius;
    public Integer speed;

    public Cell(){
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
