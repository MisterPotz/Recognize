package com.gornostaev.recognize.connection.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//java-репрезентация json'a одной картинки
public class ImageRequest {

    @SerializedName("image")
    @Expose
    private ImageSource image;

    @SerializedName("features")
    @Expose
    private List<Feature> features = null;
    @SerializedName("imageContext")
    @Expose
    private String imageContext = null;

    public String getImageContext() {
        return imageContext;
    }

    public void setImageContext(String imageContext) {
        this.imageContext = imageContext;
    }

    public ImageSource getImageSource() {
        return image;
    }

    public void setImageSource(ImageSource image) {
        this.image = image;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }


}