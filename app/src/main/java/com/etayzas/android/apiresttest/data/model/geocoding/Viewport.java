package com.etayzas.android.apiresttest.data.model.geocoding;

/**
 * Created by Ewise on 19/02/2018.
 */

import java.util.HashMap;
import java.util.Map;

import com.etayzas.android.apiresttest.data.model.geocoding.Northeast_;
import com.etayzas.android.apiresttest.data.model.geocoding.Southwest_;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "northeast",
        "southwest"
})
public class Viewport {

    @JsonProperty("northeast")
    private Northeast_ northeast;
    @JsonProperty("southwest")
    private Southwest_ southwest;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("northeast")
    public Northeast_ getNortheast() {
        return northeast;
    }

    @JsonProperty("northeast")
    public void setNortheast(Northeast_ northeast) {
        this.northeast = northeast;
    }

    @JsonProperty("southwest")
    public Southwest_ getSouthwest() {
        return southwest;
    }

    @JsonProperty("southwest")
    public void setSouthwest(Southwest_ southwest) {
        this.southwest = southwest;
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