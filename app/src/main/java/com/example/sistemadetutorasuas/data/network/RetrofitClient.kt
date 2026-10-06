package com.example.sistemadetutorasuas.data.network

import android.content.Context
import com.example.sistemadetutorasuas.data.repository.TokenManager

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // IP del host local vista desde el emulador de Android Studio
    private const val BASE_URL = "http://10.0.2.2:8001/api/"

    fun getApiService(context: Context): ApiService {
        val tokenManager = TokenManager(context)

        // Interceptor para agregar headers a todas las peticiones
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .addHeader("Accept", "application/json")

            val token = tokenManager.getToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        // Interceptor para ver los logs de red en el Logcat (depuración)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}