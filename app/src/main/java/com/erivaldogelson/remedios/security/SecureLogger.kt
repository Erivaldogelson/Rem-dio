package com.erivaldogelson.remedios.security

import android.util.Log
import com.erivaldogelson.remedios.BuildConfig

object SecureLogger {
    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message.redactSensitiveValues(), throwable)
        }
    }

    fun warning(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message.redactSensitiveValues(), throwable)
        }
    }

    private fun String.redactSensitiveValues(): String =
        replace(Regex("(?i)(bearer\\s+)[a-z0-9._\\-]+"), "$1[redacted]")
            .replace(Regex("(?i)(token|password|authorization)=([^\\s&]+)"), "$1=[redacted]")
}

object SecureErrorMessages {
    const val GENERIC = "Nao foi possivel concluir a operacao. Tente novamente."
    const val AUTH_REQUIRED = "Sua sessao expirou. Entre novamente."
    const val FORBIDDEN = "Voce nao tem permissao para acessar este recurso."
    const val UNSAFE_DEVICE = "Por seguranca, este recurso nao esta disponivel neste ambiente."
}
