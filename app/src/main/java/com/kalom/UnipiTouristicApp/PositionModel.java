package com.kalom.UnipiTouristicApp;

import android.location.Location;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PositionModel {

    private String title;
    private String desc;
    private String categ;
    private Location geoloc;

    public PositionModel(String title, String desc, String categ, Location geoloc) {
        this.title = title;
        this.desc = desc;
        this.categ = categ;
        this.geoloc = geoloc;
    }

    public PositionModel(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCateg() {
        return categ;
    }

    public void setCateg(String categ) {
        this.categ = categ;
    }

    public Location getGeoloc() {
        return geoloc;
    }

    public void setGeoloc(Location geoloc) {
        this.geoloc = geoloc;
    }
}
