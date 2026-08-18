package com.example.valomobile.di

import com.example.valomobile.data.remote.BackendApiService
import com.example.valomobile.data.remote.RiotAuthApiService
import com.example.valomobile.data.remote.RiotStoreApiService
import com.example.valomobile.data.remote.ValorantApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.example.valomobile.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRiotAuthApiService(okHttpClient: OkHttpClient, gson: Gson): RiotAuthApiService {
        return Retrofit.Builder()
            .baseUrl("https://auth.riotgames.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RiotAuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRiotStoreApiService(okHttpClient: OkHttpClient, gson: Gson): RiotStoreApiService {
        return Retrofit.Builder()
            .baseUrl("https://pd.eu.a.pvp.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RiotStoreApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideValorantApiService(okHttpClient: OkHttpClient, gson: Gson): ValorantApiService {
        return Retrofit.Builder()
            .baseUrl("https://valorant-api.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ValorantApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBackendApiService(okHttpClient: OkHttpClient, gson: Gson): BackendApiService {
        return Retrofit.Builder()
            .baseUrl("https://placeholder.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BackendApiService::class.java)
    }
}
