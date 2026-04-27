package com.erivaldogelson.remedios.auth

class AuthUsageExamples(
    private val authRepository: AuthRepository,
) {
    suspend fun loginExample(email: String, password: String): String =
        when (val result = authRepository.login(email, password)) {
            AuthResult.Success -> "Login concluido."
            is AuthResult.Blocked -> result.userMessage
            is AuthResult.Error -> result.userMessage
        }

    suspend fun logoutExample() {
        authRepository.logoutSecurely()
    }

    suspend fun authenticatedRetrofitCallExample(): String =
        when (val result = authRepository.authenticatedMedicationExample()) {
            is AuthenticatedCallResult.Success -> "Itens recebidos: ${result.value.size}"
            is AuthenticatedCallResult.Error -> result.userMessage
        }
}
