package com.example.masterknx.di

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppContextModule(
    private val context: Context
) {

    @Provides
    fun provideAppContext() = context

}