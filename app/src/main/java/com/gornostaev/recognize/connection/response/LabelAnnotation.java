package com.gornostaev.recognize.connection.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//хранит одну метку и соответствующие ей очки совпадения
public class LabelAnnotation {


    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("score")
    @Expose
    private double score;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}