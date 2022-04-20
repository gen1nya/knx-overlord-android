package com.example.masterknx.di

import com.example.masterknx.data.network.Api
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    companion object {
        private const val TIMEOUT = 120L
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(
    ): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor())
        .build()

    @Provides
    @Singleton
    internal fun provideGson(): Gson = GsonBuilder().create()

    @Singleton
    @Provides
    internal fun provideBaseRetrofit(
        gson: Gson,
        httpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.35:8000")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()

    @Singleton
    @Provides
    internal fun provideApi(
        retrofit: Retrofit
    ): Api = retrofit.create(Api::class.java)

}