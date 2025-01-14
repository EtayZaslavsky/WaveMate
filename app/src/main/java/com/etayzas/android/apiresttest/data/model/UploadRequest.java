package com.etayzas.android.apiresttest.data.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ewise on 18/01/2018.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "lat",
        "lon",
        "description",
        "crowed",
        "height",
        "goovy"
})
public class UploadRequest {

    @JsonProperty("lat")
    private double lat;
    @JsonProperty("lon")
    private double lon;
    @JsonProperty("description")
    private String description;
    @JsonProperty("crowed")
    private String crowed;
    @JsonProperty("height")
    private String height;
    @JsonProperty("goovy")
    private String goovy;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    //Initalize Fields
    public void init(double lat, double lon, String description, String height, String crowed, String goovy) {
        this.lat = lat;
        this.lon = lon;
        this.description = description;
        this.crowed = crowed;
        this.height = height;
        this.goovy = goovy;
    }

    @JsonProperty("lat")
    public double getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public void setLat(double lat) {
        this.lat = lat;
    }

    @JsonProperty("lon")
    public double getLon() {
        return lon;
    }

    @JsonProperty("lon")
    public void setLon(double lon) {
        this.lon = lon;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("crowed")
    public String getCrowed() {
        return crowed;
    }

    @JsonProperty("crowed")
    public void setCrowed(String crowed) {
        this.crowed = crowed;
    }

    @JsonProperty("height")
    public String getHeight() {
        return height;
    }

    @JsonProperty("height")
    public void setHeight(String height) {
        this.height = height;
    }

    @JsonProperty("goovy")
    public String getGoovy() {
        return goovy;
    }

    @JsonProperty("goovy")
    public void setGoovy(String goovy) {
        this.goovy = goovy;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
