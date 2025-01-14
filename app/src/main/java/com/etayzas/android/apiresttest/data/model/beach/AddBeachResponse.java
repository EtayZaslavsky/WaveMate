package com.etayzas.android.apiresttest.data.model.beach;

/**
 * Created by Ewise on 04/03/2018.
 */
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "error",
        "message",
        "beach_id",
        "beach_name"
})
public class AddBeachResponse {

    @JsonProperty("error")
    private Boolean error;
    @JsonProperty("message")
    private String message;
    @JsonProperty("beach_id")
    private Integer beachId;
    @JsonProperty("beach_name")
    private String beachName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("error")
    public Boolean getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(Boolean error) {
        this.error = error;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("beach_id")
    public Integer getBeachId() {
        return beachId;
    }

    @JsonProperty("beach_id")
    public void setBeachId(Integer beachId) {
        this.beachId = beachId;
    }

    @JsonProperty("beach_name")
    public String getBeachName() {
        return beachName;
    }

    @JsonProperty("beach_name")
    public void setBeachName(String beachName) {
        this.beachName = beachName;
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