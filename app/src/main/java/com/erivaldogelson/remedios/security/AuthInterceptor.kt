package com.erivaldogelson.remedios.security

import okhttp3.Interceptor
import okhttp3.Response

interface TokenProvider {
    fun accessToken(): String?
}

interface AuthFailureHandler {
    fun onAuthorizationFailure(statusCode: Int)
}

class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val authFailureHandler: AuthFailureHandler,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.accessToken()
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(request)
        if (response.code == HTTP_UNAUTHORIZED || response.code == HTTP_FORBIDDEN || response.hasExpiredTokenChallenge()) {
            authFailureHandler.onAuthorizationFailure(response.code)
        }
        return response
    }

    private fun Response.hasExpiredTokenChallenge(): Boolean {
        val challenge = header("WWW-Authenticate").orEmpty()
        return challenge.contains("invalid_token", ignoreCase = true) ||
            challenge.contains("expired", ignoreCase = true)
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
    }
}
