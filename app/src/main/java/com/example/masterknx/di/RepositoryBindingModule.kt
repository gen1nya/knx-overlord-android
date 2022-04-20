package com.example.masterknx.di

import com.example.masterknx.data.ApiRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface RepositoryBindingModule {

    @Binds
    @Singleton
    fun bindApiService(
       apiRepository: ApiRepository
    ): com.example.masterknx.domain.ApiRepository
}