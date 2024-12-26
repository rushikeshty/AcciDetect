package com.example.software2.dapp.SendNotification;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FcmApi {
    @POST("/v1/projects/dapp-53d6d/messages:send") // Replace YOUR_PROJECT_ID
    Call<Void> sendNotification(
            @Header("Authorization") String authorization,
            @Body FCMRequest body
    );
}
