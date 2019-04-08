package com.gornostaev.recognize.connection.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//хранит полный ответ Cloud Vision API на одну картинку
public class ImageResponse {

    @SerializedName("labelAnnotations")
    @Expose
    private List<LabelAnnotation> labelAnnotations = null;

    public List<LabelAnnotation> getLabelAnnotations() {
        return labelAnnotations;
    }

    public void setLabelAnnotations(List<LabelAnnotation> labelAnnotations) {
        this.labelAnnotations = labelAnnotations;
    }

}