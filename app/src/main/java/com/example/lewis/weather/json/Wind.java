package com.example.lewis.weather.json;

public class Wind {

    private String speed;

    private double deg;



    public Wind(String speed, double deg) {
        this.speed = speed;
        this.deg = deg;
    }

    public String getSpeed() {
        return speed;
    }

    public double getDeg() {
        return deg;
    }



}
