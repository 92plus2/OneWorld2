package com.work.project.Fragments;

import com.work.project.Notifications.MyResponse;
import com.work.project.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAETDepeU:APA91bGmoqjwVxHgQ_wn1LjcKE12wSLNliURxFUa_563X2CHvzm-sKD-Djf6V6yaeVAcRXQbZ2Y0iLPupvmzRHNEEBvvK4kECt2rlizFaAgdsVSnJNhGB_d6FtEVKlvtobqoKHTCXo7e"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
