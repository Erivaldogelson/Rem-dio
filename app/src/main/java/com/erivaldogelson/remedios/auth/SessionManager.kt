package com.erivaldogelson.remedios.auth

import com.erivaldogelson.remedios.network.SessionResponse
import com.erivaldogelson.remedios.security.AuthFailureHandler
import com.erivaldogelson.remedios.security.SecurePrefsManager
import com.erivaldogelson.remedios.security.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val securePrefsManager: SecurePrefsManager,
) : TokenProvider, AuthFailureHandler {
    private val _sessionState = MutableStateFlow(
        if (securePrefsManager.accessToken().isNullOrBlank()) SessionState.SignedOut else SessionState.SignedIn,
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    override fun accessToken(): String? = securePrefsManager.accessToken()

    override fun onAuthorizationFailure(statusCode: Int) {
        securePrefsManager.clearAuthTokens()
        _sessionState.value = when (statusCode) {
            401 -> SessionState.Expired
            403 -> SessionState.Forbidden
            else -> SessionState.Expired
        }
    }

    fun saveAuthenticatedSession(accessToken: String, refreshToken: String?) {
        securePrefsManager.saveAuthTokens(accessToken, refreshToken)
        _sessionState.value = SessionState.SignedIn
    }

    fun applyServerSession(session: SessionResponse) {
        _sessionState.value = if (session.active) {
            SessionState.SignedIn
        } else {
            securePrefsManager.clearAuthTokens()
            SessionState.Expired
        }
    }

    fun hasServerPermission(session: SessionResponse, permission: String): Boolean =
        session.permissions.contains(permission)

    fun hasServerPlan(session: SessionResponse, requiredPlan: String): Boolean =
        session.plan == requiredPlan

    fun logoutSecurely() {
        securePrefsManager.clearAllSensitiveData()
        _sessionState.value = SessionState.SignedOut
    }
}

enum class SessionState {
    SignedOut,
    SignedIn,
    Expired,
    Forbidden,
}
