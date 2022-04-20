package com.example.masterknx.ui

import android.util.Log
import com.example.masterknx.Application
import com.example.masterknx.R
import com.example.masterknx.data.network.EventResponse
import com.example.masterknx.domain.ApiRepository
import com.example.masterknx.domain.Page
import com.example.masterknx.domain.SmartHomeDeviceType
import com.example.masterknx.domain.SocketConnectionState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import javax.inject.Inject

class DevicesPm(
    private val page: Page
) : BasePm() {

    init {
        Application.appComponent.inject(this)
    }

    @Inject
    lateinit var apiRepository: ApiRepository

    val content = command<List<DevicesAdapter.Item>>()
    val socketConnectionStatus = command<SocketConnectionState>()

    val itemChanged = action<DevicesAdapter.Item> {
        switchMap { item ->
            when(item.device.type) {
                SmartHomeDeviceType.SWITCH -> {
                    apiRepository.setDigitalValue(item.device.id, item.isEnabled)
                }
                SmartHomeDeviceType.ANALOG -> {
                    apiRepository.setAnalogValue(item.device.id, item.device.value)
                }
                SmartHomeDeviceType.ANALOG_WITHOUT_DIMMING -> {
                    apiRepository.toggleAnalogWithoutDimming(item.device.id, item.isEnabled)
                }
            }
                .toObservable<Any>()
        }
    }

    override fun onBind() {
        super.onBind()
        Observable.combineLatest(
            apiRepository.socketListener,
            apiRepository.getDevices().toObservable()
        ) { socketEvent, items ->
            val filterdItems = items.filter { it.page == page }
            .map { device ->
                DevicesAdapter.Item(device = device, device.value > 0.0)
            }

            val newItems = mutableListOf<DevicesAdapter.Item>()
            newItems.addAll(filterdItems.map { it.copy(device = it.device.copy(
                id = it.device.id,
                type = it.device.type,
                name = it.device.name,
                page = it.device.page,
                kind = it.device.kind,
                value = it.device.value),
                isEnabled = it.isEnabled
            ) })
            newItems.find { it.device.id == socketEvent.data.id }?.let {
                it.device.value = socketEvent.data.data?.toDoubleOrNull() ?: 0.0
                it.isEnabled = it.device.value > 0.0
            }
            newItems
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                content.accept(it)
            }, {
                it.printStackTrace()
            })
            .untilUnbind()

        apiRepository.connectionStatusRelay
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                socketConnectionStatus.accept(it)
                Log.d("connection status", it.toString())
            }, {
                it.printStackTrace()
            })
            .untilUnbind()
    }
}