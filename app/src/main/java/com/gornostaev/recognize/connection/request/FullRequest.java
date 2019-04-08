package com.gornostaev.recognize.connection.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//полная java репрезентация запроса на Cloud Vision API
public class FullRequest {

    @SerializedName("requests")
    @Expose
    private List<ImageRequest> requests = null;

    public List<ImageRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<ImageRequest> requests) {
        this.requests = requests;
    }

    //билдер запроса, принимающий картинку в base64
    //создает json-часть запроса в Cloud Vision API для одной картинки
    public static FullRequest buildFullRequest(String base64) {
        FullRequest data = new FullRequest();
        List<ImageRequest> list = new ArrayList<>();
        ImageRequest request_per_image = new ImageRequest();
        ImageSource imageSource = new ImageSource();
        imageSource.setContent(base64);
        request_per_image.setImageSource(imageSource);
        List<Feature> features_list = new ArrayList<>();
        features_list.add(new Feature());
        request_per_image.setFeatures(features_list);
        list.add(request_per_image);
        data.setRequests(list);
        return data;
    }
}