package com.example.masterknx.data.network

import com.google.gson.annotations.SerializedName

class EventResponse (
    @SerializedName("data")
    val data: Data = Data(),
    @SerializedName("event")
    val event: String? = ""
) {

    companion object {
        const val UI_STATE_EVENT = "UI-State"
        const val UI_ANALOG_EVENT = "UI-Analog"
        const val UI_TEXT_EVENT = "UI-Text"

    }

    //response: {data:{data:0.0,id:-609925},context:"",time:20220410175534,event:"UI-Analog"}
    data class Data (
        @SerializedName("data")
        val data: String? = "",
        @SerializedName("id")
        val id: Int? = -1
    )

}