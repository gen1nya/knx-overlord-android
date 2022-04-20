package com.example.masterknx.domain

import com.example.masterknx.data.ApiRepository
import com.example.masterknx.data.network.EventResponse
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface ApiRepository {
    fun getDevices(): Single<List<SmartHomeDevice>>
    fun toggleDevice(id: Int): Completable
    fun setDigitalValue(id: Int, enable: Boolean): Completable
    fun setAnalogValue(id: Int, value: Double): Completable
    fun toggleAnalogWithoutDimming(id: Int, enable: Boolean): Completable

    val connectionStatusRelay: BehaviorRelay<SocketConnectionState>
    val socketListener: BehaviorRelay<EventResponse>
}