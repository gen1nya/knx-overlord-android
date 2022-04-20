package com.example.masterknx.data.network

import com.example.masterknx.domain.SmartHomeDevice
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    companion object {
        private const val server = "http://192.168.1.35:8000"
    }

    @GET("server/devices/")
    fun getDevices(): Single<List<SmartHomeDevice>>

    @GET("device/toggle")
    fun toggle(
        @Query("id")
        id: Int
    ): Single<ActionResponse>

    @GET("device/switch")
    fun switch(
        @Query("id")
        id: Int,
        @Query("enable")
        enable: String
    ): Single<ActionResponse>

   @GET("device/analog")
    fun analog(
        @Query("id")
        id: Int,
        @Query("value")
        analogValue: Double
    ): Single<ActionResponse>



}