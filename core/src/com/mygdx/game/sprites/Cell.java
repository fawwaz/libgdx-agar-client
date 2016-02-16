package com.mygdx.game.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Asus on 15/02/2016.
 */
public class Cell{
    Vector2 previousposition;
    public Double mass;
    public double x;
    public double y;
    public Double radius;
    public Integer speed;

    public Cell(){
        previousposition = new Vector2();
        previousposition.set(getX(),getY());
    }

    public Cell(Double mass,double x, double y, Double radius, Integer speed){
        this.mass = mass;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speed = speed;
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

    public Vector2 getPreviousposition() {
        return previousposition;
    }

    public void setPreviousposition(Vector2 previousposition) {
        this.previousposition = previousposition;
    }

    public Double getMass() {
        return mass;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }

    public float getX() {
        return (float) x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public float getY() {
        return (float) y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }
}
