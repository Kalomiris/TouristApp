package com.kalom.UnipiTouristicApp;

import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Objects;

@IgnoreExtraProperties
public class PositionModel implements Serializable {

    private String title;
    private String desc;
    private String categ;
    private double latitude;
    private double longtitude;

    public PositionModel(String title, String desc, String categ, double latitude, double longtitude) {
        this.title = title;
        this.desc = desc;
        this.categ = categ;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public PositionModel() {
    }

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    @Override
    public String toString() {
        return "PositionModel{" +
                "title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", categ='" + categ + '\'' +
                ", latitude=" + latitude +
                ", longtitude=" + longtitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionModel)) return false;
        PositionModel that = (PositionModel) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longtitude, longtitude) == 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longtitude);
    }
}
