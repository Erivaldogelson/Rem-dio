package com.erivaldogelson.remedios.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    // Replace these paths with the real backend endpoints.
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("auth/session")
    suspend fun session(): Response<SessionResponse>

    @GET("medications/secure")
    suspend fun authenticatedMedicationExample(): Response<List<RemoteMedicationDto>>
}

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("device_integrity") val deviceIntegrity: DeviceIntegrityDto,
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresInSeconds: Long?,
    @SerializedName("user") val user: RemoteUserDto?,
)

data class SessionResponse(
    @SerializedName("active") val active: Boolean,
    @SerializedName("plan") val plan: String?,
    @SerializedName("permissions") val permissions: List<String> = emptyList(),
)

data class DeviceIntegrityDto(
    @SerializedName("trusted_for_sensitive_actions") val trustedForSensitiveActions: Boolean,
    @SerializedName("risk_signals") val riskSignals: List<String>,
    @SerializedName("play_integrity_state") val playIntegrityState: String,
)

data class RemoteUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?,
)

data class RemoteMedicationDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("dosage") val dosage: String?,
)
