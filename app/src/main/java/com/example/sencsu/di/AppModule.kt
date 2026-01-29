package com.example.sencsu.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.AuthInterceptor
import com.example.sencsu.data.repository.SessionManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Extension DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient() // Permet d'être plus tolérant sur le format JSON
        .create()

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor =
        AuthInterceptor(sessionManager)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        // Logging : Utile en développement, à limiter en production
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)

            // --- 🔧 FIX BROKEN PIPE & TIMEOUTS ---
            .connectTimeout(30, TimeUnit.SECONDS) // Temps pour établir la connexion
            .readTimeout(30, TimeUnit.SECONDS)    // Temps pour recevoir les données
            .writeTimeout(30, TimeUnit.SECONDS)   // Temps pour envoyer les données

            // --- 🔄 FIABILITÉ ---
            .retryOnConnectionFailure(true)       // Reconnexion auto en cas de micro-coupure
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson // Utilise l'instance Gson fournie au-dessus
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Injection de l'instance Gson
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}