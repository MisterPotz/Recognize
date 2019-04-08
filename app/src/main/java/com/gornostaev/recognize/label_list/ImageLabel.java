package com.gornostaev.recognize.label_list;

import android.os.Parcel;
import android.os.Parcelable;

import com.gornostaev.recognize.connection.response.FullResponse;
import com.gornostaev.recognize.connection.response.ImageResponse;
import com.gornostaev.recognize.connection.response.LabelAnnotation;

import java.util.ArrayList;

//класс для хранения метки label и соответствующие ей очки совпадения
public class ImageLabel implements Parcelable {
    //нужно для получения массива ImageLabel из ответа CLoud Vision API
    public static ImageLabel[] getData(FullResponse response) {
        //получаем первый элемент, т.к. мы посылаем только одну картинку
        //в Cloud Vision API
        ImageResponse imageResponse = response.getResponses().get(0);
        //получаем список меток и очков для первой картинки
        ArrayList<LabelAnnotation> annotation_first = (ArrayList<LabelAnnotation>) imageResponse.getLabelAnnotations();
        //размер списка
        int size = annotation_first.size();
        //и заполняем массив imageLabels
        ImageLabel[] imageLabels = new ImageLabel[size];
        for (int i = 0; i < size; i++) {
            imageLabels[i] = new ImageLabel(annotation_first.get(i).getDescription(),
                    annotation_first.get(i).getScore());
        }
        return imageLabels;
    }

    //для создания ImageLabel непосредственно из части LabelAnnotation ответа
    //Cloud Vision API
    public ImageLabel(LabelAnnotation annotation) {
        label = annotation.getDescription();
        score = annotation.getScore();
    }

    private String label;
    private double score;

    public ImageLabel(String label, double score) {
        setLabel(label);
        setScore(score);
    }

    public ImageLabel(Parcel in) {
        this.label = in.readString();
        this.score = in.readDouble();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public static final Parcelable.Creator<ImageLabel> CREATOR
            = new Parcelable.Creator<ImageLabel>() {
        public ImageLabel createFromParcel(Parcel in) {
            return new ImageLabel(in);
        }

        public ImageLabel[] newArray(int size) {
            return new ImageLabel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.label);
        dest.writeDouble(this.score);
    }
}
