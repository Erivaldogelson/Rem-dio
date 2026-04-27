package com.erivaldogelson.remedios.network

import com.erivaldogelson.remedios.BuildConfig
import com.erivaldogelson.remedios.security.AuthFailureHandler
import com.erivaldogelson.remedios.security.AuthInterceptor
import com.erivaldogelson.remedios.security.CertificatePinningProvider
import com.erivaldogelson.remedios.security.TokenProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(
        tokenProvider: TokenProvider,
        authFailureHandler: AuthFailureHandler,
        baseUrl: String = BuildConfig.API_BASE_URL,
    ): ApiService {
        require(baseUrl.startsWith("https://")) {
            "API_BASE_URL must use HTTPS. Configure DEBUG_API_BASE_URL or RELEASE_API_BASE_URL."
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .certificatePinner(CertificatePinningProvider.fromBuildConfig())
            .addInterceptor(AuthInterceptor(tokenProvider, authFailureHandler))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            // BASIC avoids logging request/response bodies that may contain sensitive data.
                            level = HttpLoggingInterceptor.Level.BASIC
                        },
                    )
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
