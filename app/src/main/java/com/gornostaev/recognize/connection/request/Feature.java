package com.gornostaev.recognize.connection.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//java-репрезентация фич картинки
//default: type = "LABEL_DETECTION
//default: maxResults = 10
public class Feature {

    @SerializedName("type")
    @Expose
    private String type = "LABEL_DETECTION";
    @SerializedName("maxResults")
    @Expose
    private int maxResults = 10;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

}