package com.mygdx.game.sprites;

/**
 * Created by Asus on 16/02/2016.
 */
public class Food {
    public String id;
    public double x;
    public double y;
    public Double radius;
    public Double mass;
    public Integer hue;

    public Food(String id, double x, double y, Double radius, Double mass, Integer hue) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.mass = mass;
        this.hue = hue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
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

    public Double getMass() {
        return mass;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }

    public Integer getHue() {
        return hue;
    }

    public void setHue(Integer hue) {
        this.hue = hue;
    }
}
