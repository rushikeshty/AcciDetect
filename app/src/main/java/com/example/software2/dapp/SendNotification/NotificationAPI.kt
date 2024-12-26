package com.example.software2.dapp.SendNotification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NotificationAPI {
    private var retrofit: Retrofit? = null
    fun sendNotification(): FcmApi {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(FcmApi::class.java)
    }
}