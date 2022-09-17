package com.example.obd;

import java.io.Serializable;

public class Trip implements Serializable {
    private Integer speedMax;
    private Integer acceleration;
    private Integer slowdown;
    private Integer turn;
    private String user;
    private String time;

    public Integer getSpeedMax() {
        return speedMax;
    }

    public void setSpeedMax(Integer speedMax) {
        this.speedMax = speedMax;
    }

    public Integer getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Integer acceleration) {
        this.acceleration = acceleration;
    }

    public Integer getSlowdown() {
        return slowdown;
    }

    public void setSlowdown(Integer slowdown) {
        this.slowdown = slowdown;
    }

    public Integer getTurn() {
        return turn;
    }

    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
