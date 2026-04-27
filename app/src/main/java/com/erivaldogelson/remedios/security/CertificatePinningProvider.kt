package com.erivaldogelson.remedios.security

import com.erivaldogelson.remedios.BuildConfig
import okhttp3.CertificatePinner

object CertificatePinningProvider {
    fun fromBuildConfig(): CertificatePinner =
        create(
            host = BuildConfig.API_HOST,
            pinsCsv = BuildConfig.CERTIFICATE_PINS,
        )

    fun create(host: String, pinsCsv: String): CertificatePinner {
        val builder = CertificatePinner.Builder()
        pinsCsv.split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { pin ->
                // Insert pins as "sha256/BASE64_HASH" in local.properties or environment variables.
                builder.add(host, pin)
            }
        return builder.build()
    }
}
