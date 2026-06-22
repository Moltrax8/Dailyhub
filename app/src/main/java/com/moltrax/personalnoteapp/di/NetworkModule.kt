package com.moltrax.personalnoteapp.di

import com.moltrax.personalnoteapp.BuildConfig
import com.moltrax.personalnoteapp.data.remote.exercisedb.ExerciseDbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Provides @Singleton
    fun provideExerciseDbApi(json: Json, http: OkHttpClient): ExerciseDbApi {
        val exerciseHttp = http.newBuilder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .apply { if (BuildConfig.EXERCISEDB_KEY.isNotEmpty()) header("X-RapidAPI-Key", BuildConfig.EXERCISEDB_KEY) }
                    .build()
                chain.proceed(req)
            }
            .build()
        return Retrofit.Builder()
            .baseUrl("https://exercisedb.p.rapidapi.com/")
            .client(exerciseHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ExerciseDbApi::class.java)
    }
}
