package com.example.masterknx.di

import com.example.masterknx.Application
import com.example.masterknx.ui.DevicesFragment
import com.example.masterknx.ui.DevicesPm
import com.example.masterknx.ui.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppContextModule::class,
        RepositoryBindingModule::class,
        NetworkModule::class
    ]
)
interface ApplicationComponent {
    fun inject(application: Application)
    fun inject(application: MainActivity)
    fun inject(devicesFragment: DevicesFragment)
    fun inject(devicesPm: DevicesPm)

}