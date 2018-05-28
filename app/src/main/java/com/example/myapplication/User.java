package com.example.myapplication;

import java.io.Serializable;

/**
 * Created by Domirae on 2018-05-09.
 */

public class User implements Serializable{

    String id;
    String fruit;
    String start_date;
    String growth_span;
    boolean today_register;
    String recent_tem;
    int fruit_growthDay;

    public int getFruit_growthDay() {
        return fruit_growthDay;
    }

    public void setFruit_growthDay(int fruit_growthDay) {
        this.fruit_growthDay = fruit_growthDay;
    }

    public boolean isToday_register() {
        return today_register;
    }
    public User(String id){
        this.id = id;
    }

    public User(String id,String fruit,String start_date){
        this.id = id;
        this.fruit = fruit;
        this.start_date = start_date;
    }

    public void setToday_register(boolean today_register) {
        this.today_register = today_register;
    }

    public String getGrowth_span() {
        return growth_span;
    }

    public void setGrowth_span(String growth_span) {
        this.growth_span = growth_span;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFruit() {
        return fruit;
    }

    public void setFruit(String fruit) {
        this.fruit = fruit;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getRecent_tem() {
        return recent_tem;
    }

    public void setRecent_tem(String recent_tem) {
        this.recent_tem = recent_tem;
    }
}
