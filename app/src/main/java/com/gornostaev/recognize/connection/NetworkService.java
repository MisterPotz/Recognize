package com.gornostaev.recognize.connection;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//класс для работы с Retrofit
public class NetworkService {
    private static NetworkService mInstance;
    //ссылка для работы с Cloud Vision API
    private static final String BASE_URL = "https://vision.googleapis.com";
    private Retrofit mRetrofit;

    private NetworkService() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    //Singleton
    public static NetworkService getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkService();
        }
        return mInstance;
    }

    //связываем NetworkService и класс, репрезентирующий
    //АПИ конечного сервера, с которого будем получать
    //ответ
    public CloudVisionApi getCloudVisionApi() {
        return mRetrofit.create(CloudVisionApi.class);
    }
}
