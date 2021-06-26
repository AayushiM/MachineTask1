package com.magnet.Network

import com.app.baazigar.network.Constant
import com.savefood.nearfood.Network.response.DataModel
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiInterface {


    @GET(Constant.DATA)
    fun login(@Body body: RequestBody?): Call<DataModel?>?





}