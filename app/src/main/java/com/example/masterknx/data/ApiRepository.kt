package com.example.masterknx.data

import com.example.masterknx.data.network.Api
import com.example.masterknx.data.network.EventResponse
import com.example.masterknx.domain.ApiRepository
import com.example.masterknx.domain.SmartHomeDevice
import com.example.masterknx.domain.SocketConnectionState
import com.google.gson.Gson
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import kotlin.concurrent.thread

class ApiRepository @Inject constructor(
    private val api: Api
): ApiRepository {

    override val connectionStatusRelay: BehaviorRelay<SocketConnectionState> = BehaviorRelay.createDefault(SocketConnectionState.DISCONNECTED)
    override val socketListener: BehaviorRelay<EventResponse> = BehaviorRelay.createDefault(EventResponse())


    init {
        thread {
            while (true) {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(InetAddress.getByName("192.168.1.35"), 8082))
                    connectionStatusRelay.accept(SocketConnectionState.CONNECTED)
                    val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                    while (true) {
                        val message = input.readLine()
                        if (message?.startsWith("{data:{data:") == true) {
                            socketListener.accept(
                                Gson().fromJson(
                                    message,
                                    EventResponse::class.java
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    connectionStatusRelay.accept(SocketConnectionState.DISCONNECTED)
                    e.printStackTrace()
                    connectionStatusRelay.accept(SocketConnectionState.CONNECTING)
                }
            }
        }
    }

    override fun getDevices(): Single<List<SmartHomeDevice>> = api.getDevices()
        .subscribeOn(Schedulers.io())

    override fun toggleDevice(id: Int): Completable = api.toggle(id)
        .subscribeOn(Schedulers.io())
        .ignoreElement()

    override fun setDigitalValue(id: Int, enable: Boolean): Completable =
        api.switch(id, enable.toString())
            .subscribeOn(Schedulers.io())
            .ignoreElement()

    override fun setAnalogValue(id: Int, value: Double) : Completable = api.analog(id, value)
        .subscribeOn(Schedulers.io())
        .ignoreElement()

    override fun toggleAnalogWithoutDimming(id: Int, enable: Boolean) : Completable {
        val analogValue = if (enable) 100.0 else 0.0
        return api.analog(id, analogValue)
            .subscribeOn(Schedulers.io())
            .ignoreElement()
    }
}