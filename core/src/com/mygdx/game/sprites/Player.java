package com.mygdx.game.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Asus on 15/02/2016.
 */
public class Player{
    // Used for checking whether the position is changed or not
    public Vector2 previousposition;
    // Clean attributes
    public String id;
    public double x;
    public double y;
    public Array<Cell> cells;
    public Double massTotal;
    public Integer hue;
    public Long lastHeartbeat;
    public Integer target_x;
    public Integer target_y;
    public String name;
    public Integer screenWidth;
    public Integer screenHeight;
    public Long lastSplit;

    public Player(String id, String name, Double x,Double y){
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        previousposition = new Vector2(getX(),getY());
    }

    public boolean hasmoved(){
        Vector2 target = new Vector2((float) target_x,(float) target_y);

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getX() {
        return (float) x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Float getY() {
        return (float) y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Array<Cell> getCells() {
        return cells;
    }

    public void setCells(Array<Cell> cells) {
        this.cells = cells;
    }

    public Double getMassTotal() {
        return massTotal;
    }

    public void setMassTotal(Double massTotal) {
        this.massTotal = massTotal;
    }

    public Integer getHue() {
        return hue;
    }

    public void setHue(Integer hue) {
        this.hue = hue;
    }

    public Long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Integer getTarget_x() {
        return target_x;
    }

    public void setTarget_x(Integer target_x) {
        this.target_x = target_x;
    }

    public Integer getTarget_y() {
        return target_y;
    }

    public void setTarget_y(Integer target_y) {
        this.target_y = target_y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Long getLastSplit() {
        return lastSplit;
    }

    public void setLastSplit(Long lastSplit) {
        this.lastSplit = lastSplit;
    }

    @Override
    public String toString() {
        /*
        Gdx.app.log("MyGame","== Player properties ==");
        Gdx.app.log("MyGame","id \t\t\t\t: "+id);
        Gdx.app.log("MyGame","name \t\t\t: "+name);
        Gdx.app.log("MyGame","x \t\t\t\t: "+x);
        Gdx.app.log("MyGame","y \t\t\t\t: "+y);
        Gdx.app.log("MyGame","cells \t\t\t: "+cells);
        Gdx.app.log("MyGame","masstotal \t\t\t: "+massTotal);
        Gdx.app.log("MyGame","hue \t\t\t\t: "+hue);
        Gdx.app.log("MyGame","target_x \t\t: "+target_x);
        Gdx.app.log("MyGame","target_y \t\t: "+target_y);
        Gdx.app.log("MyGame","lastheartbeat\t: "+lastHeartbeat);
        Gdx.app.log("MyGame","lastsplit \t\t: "+lastSplit);
        Gdx.app.log("MyGame","screenwidth \t: "+screenWidth);
        Gdx.app.log("MyGame","screenheight \t: "+screenHeight);
        */
        return "Ntar dihandle ya... ini masih mockup";
    }
}
