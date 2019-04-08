package com.gornostaev.recognize.label_list;

import android.os.Parcel;
import android.os.Parcelable;

import com.gornostaev.recognize.connection.response.FullResponse;

//хранит полный массив меток и очков совпадений для одной картинки
public class ContainerImageLabels implements Parcelable {
    private ImageLabel[] imageLabels;

    public ContainerImageLabels(ImageLabel[] imageLabels) {
        this.imageLabels = imageLabels;
    }

    public ContainerImageLabels(Parcel in) {
        this.imageLabels = in.createTypedArray(ImageLabel.CREATOR);

    }

    public ContainerImageLabels(FullResponse fullResponse) {
        imageLabels = ImageLabel.getData(fullResponse);
    }


    public static final Parcelable.Creator<ContainerImageLabels> CREATOR
            = new Parcelable.Creator<ContainerImageLabels>() {
        public ContainerImageLabels createFromParcel(Parcel in) {
            return new ContainerImageLabels(in);
        }

        public ContainerImageLabels[] newArray(int size) {
            return new ContainerImageLabels[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(imageLabels, 0);
    }

    public ImageLabel[] getImageLabels() {
        return imageLabels;
    }
}
