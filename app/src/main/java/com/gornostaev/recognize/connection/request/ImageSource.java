package com.gornostaev.recognize.connection.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//хранит source картинки (base64  String)
public class ImageSource {
    @SerializedName("content")
    @Expose
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}