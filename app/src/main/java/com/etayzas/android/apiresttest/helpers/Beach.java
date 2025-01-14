package com.etayzas.android.apiresttest.helpers;

/**
 * Created by Ewise on 16/02/2018.
 */

public class Beach {
    private double lat;
    private double lon;
    private String name;

    public Beach(double lat, double lon, String name)
    {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    public double getLat() {return lat;}
    public double getLon() {return lon;}
    public String getName() {return name;}

    public void setLat(double lat) {this.lat = lat;}
    public void setLon(double lon) {this.lon = lon;}
    public void setName(String name) {this.name = name;}
}
