package com.example.masterknx.data.network

import com.google.gson.annotations.SerializedName

class ActionResponse(
    @SerializedName("exitCode")
    val exitCode: Int?,
    @SerializedName("trans")
    val trans: String?,
    @SerializedName("out")
    val out: ArrayList<String>?
)