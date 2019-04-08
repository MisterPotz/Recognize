package com.gornostaev.recognize.connection;

import com.gornostaev.recognize.connection.request.FullRequest;
import com.gornostaev.recognize.connection.response.FullResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

//интерфейс для запросов в google cloud vision api
public interface CloudVisionApi {
    //только один запрос POST. принимает ключ-АПИ, тело запроса FullRequest
    //и ожидает ответ FullResponse
    @POST("/v1/images:annotate")
    public Call<FullResponse> postData(@Query("key") String apiKey, @Body FullRequest data);
}

