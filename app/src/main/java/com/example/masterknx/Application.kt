package com.example.masterknx

import android.app.Application
import com.example.masterknx.di.AppContextModule
import com.example.masterknx.di.ApplicationComponent
import com.example.masterknx.di.DaggerApplicationComponent

class Application: Application() {

    companion object {
        lateinit var appComponent: ApplicationComponent
        private set
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerApplicationComponent.builder()
            .appContextModule(AppContextModule(applicationContext))
            .build()
            .also {
                it.inject(this)
            }
    }

}