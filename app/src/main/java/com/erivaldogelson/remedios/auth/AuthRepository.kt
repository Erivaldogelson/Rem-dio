package com.erivaldogelson.remedios.auth

import com.erivaldogelson.remedios.network.ApiService
import com.erivaldogelson.remedios.network.DeviceIntegrityDto
import com.erivaldogelson.remedios.network.LoginRequest
import com.erivaldogelson.remedios.network.RemoteMedicationDto
import com.erivaldogelson.remedios.network.SessionResponse
import com.erivaldogelson.remedios.security.DeviceIntegrityChecker
import com.erivaldogelson.remedios.security.SecureErrorMessages
import com.erivaldogelson.remedios.security.SecureLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val deviceIntegrityChecker: DeviceIntegrityChecker,
) {
    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val verdict = deviceIntegrityChecker.currentVerdict()
        if (!verdict.isTrustedForSensitiveActions) {
            return@withContext AuthResult.Blocked(SecureErrorMessages.UNSAFE_DEVICE)
        }

        try {
            val response = apiService.login(
                LoginRequest(
                    email = email,
                    password = password,
                    deviceIntegrity = DeviceIntegrityDto(
                        trustedForSensitiveActions = verdict.isTrustedForSensitiveActions,
                        riskSignals = verdict.signals.map { it.name },
                        playIntegrityState = verdict.playIntegrityState.name,
                    ),
                ),
            )

            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body?.accessToken.isNullOrBlank()) {
                        AuthResult.Error(SecureErrorMessages.GENERIC)
                    } else {
                        sessionManager.saveAuthenticatedSession(
                            accessToken = body.accessToken,
                            refreshToken = body.refreshToken,
                        )
                        AuthResult.Success
                    }
                }
                response.code() == 401 -> AuthResult.Error(SecureErrorMessages.AUTH_REQUIRED)
                response.code() == 403 -> AuthResult.Error(SecureErrorMessages.FORBIDDEN)
                else -> AuthResult.Error(SecureErrorMessages.GENERIC)
            }
        } catch (throwable: Throwable) {
            SecureLogger.warning(TAG, "Login request failed", throwable)
            AuthResult.Error(SecureErrorMessages.GENERIC)
        }
    }

    suspend fun verifySessionWithServer(): SessionCheckResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.session()
            if (response.isSuccessful) {
                val session = response.body()
                if (session != null) {
                    sessionManager.applyServerSession(session)
                    SessionCheckResult.Success(session)
                } else {
                    SessionCheckResult.Error(SecureErrorMessages.GENERIC)
                }
            } else if (response.code() == 401) {
                sessionManager.onAuthorizationFailure(response.code())
                SessionCheckResult.Error(SecureErrorMessages.AUTH_REQUIRED)
            } else if (response.code() == 403) {
                sessionManager.onAuthorizationFailure(response.code())
                SessionCheckResult.Error(SecureErrorMessages.FORBIDDEN)
            } else {
                SessionCheckResult.Error(SecureErrorMessages.GENERIC)
            }
        } catch (throwable: Throwable) {
            SecureLogger.warning(TAG, "Session verification failed", throwable)
            SessionCheckResult.Error(SecureErrorMessages.GENERIC)
        }
    }

    suspend fun logoutSecurely() = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
        } catch (throwable: Throwable) {
            SecureLogger.debug(TAG, "Remote logout failed; clearing local session anyway", throwable)
        } finally {
            sessionManager.logoutSecurely()
        }
    }

    suspend fun authenticatedMedicationExample(): AuthenticatedCallResult<List<RemoteMedicationDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.authenticatedMedicationExample()
                when {
                    response.isSuccessful -> AuthenticatedCallResult.Success(response.body().orEmpty())
                    response.code() == 401 -> {
                        sessionManager.onAuthorizationFailure(response.code())
                        AuthenticatedCallResult.Error(SecureErrorMessages.AUTH_REQUIRED)
                    }
                    response.code() == 403 -> {
                        sessionManager.onAuthorizationFailure(response.code())
                        AuthenticatedCallResult.Error(SecureErrorMessages.FORBIDDEN)
                    }
                    else -> AuthenticatedCallResult.Error(SecureErrorMessages.GENERIC)
                }
            } catch (throwable: Throwable) {
                SecureLogger.warning(TAG, "Authenticated medication call failed", throwable)
                AuthenticatedCallResult.Error(SecureErrorMessages.GENERIC)
            }
        }

    private companion object {
        const val TAG = "AuthRepository"
    }
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val userMessage: String) : AuthResult
    data class Blocked(val userMessage: String) : AuthResult
}

sealed interface SessionCheckResult {
    data class Success(val session: SessionResponse) : SessionCheckResult
    data class Error(val userMessage: String) : SessionCheckResult
}

sealed interface AuthenticatedCallResult<out T> {
    data class Success<T>(val value: T) : AuthenticatedCallResult<T>
    data class Error(val userMessage: String) : AuthenticatedCallResult<Nothing>
}
