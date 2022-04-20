package com.example.masterknx.domain

data class SmartHomeDevice(
    val id: Int,
    val type: SmartHomeDeviceType,
    val name: String,
    val page: Page,
    val kind: DeviceKind,
    var value: Double = 0.0,
)

enum class Page {
    MASTER_BEDROOM, LOUNGE;
}