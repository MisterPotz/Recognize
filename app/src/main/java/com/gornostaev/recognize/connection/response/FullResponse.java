package com.gornostaev.recognize.connection.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//хранит шаблон полного ответа Cloud Vision API
public class FullResponse {

    @SerializedName("responses")
    @Expose
    private List<ImageResponse> responses = null;

    public List<ImageResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<ImageResponse> responses) {
        this.responses = responses;
    }

}