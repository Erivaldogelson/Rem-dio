package com.erivaldogelson.remedios.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.erivaldogelson.remedios.BuildConfig
import java.io.File
import java.security.MessageDigest
import java.util.Locale

class DeviceIntegrityChecker(
    private val context: Context,
) {
    fun currentVerdict(): DeviceIntegrityVerdict {
        val signals = buildSet {
            if (isAppDebuggable()) add(DeviceRiskSignal.DEBUGGABLE_BUILD)
            if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) add(DeviceRiskSignal.DEBUGGER_ATTACHED)
            if (isProbablyRooted()) add(DeviceRiskSignal.ROOT_SIGNALS)
            if (isProbablyEmulator()) add(DeviceRiskSignal.EMULATOR_SIGNALS)
            if (hasUnexpectedAppSignature()) add(DeviceRiskSignal.APK_SIGNATURE_MISMATCH)
        }
        return DeviceIntegrityVerdict(
            isTrustedForSensitiveActions = signals.isEmpty(),
            signals = signals,
            playIntegrityState = PlayIntegrityState.NOT_CONFIGURED,
        )
    }

    suspend fun requestPlayIntegrityVerdict(nonce: String): PlayIntegrityState {
        // Wire the official Play Integrity API here when the app has a Google Cloud project.
        // Send the token to your backend and validate it on the server. Do not trust only local checks.
        return if (nonce.isBlank()) PlayIntegrityState.NOT_CONFIGURED else PlayIntegrityState.NOT_CONFIGURED
    }

    private fun isAppDebuggable(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun isProbablyRooted(): Boolean {
        val buildTags = Build.TAGS.orEmpty()
        val hasTestKeys = buildTags.contains("test-keys")
        val suspiciousFiles = ROOT_PATHS.any { File(it).exists() }
        return hasTestKeys || suspiciousFiles
    }

    private fun isProbablyEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.orEmpty().lowercase()
        val model = Build.MODEL.orEmpty().lowercase()
        val manufacturer = Build.MANUFACTURER.orEmpty().lowercase()
        val brand = Build.BRAND.orEmpty().lowercase()
        val device = Build.DEVICE.orEmpty().lowercase()
        val product = Build.PRODUCT.orEmpty().lowercase()

        return fingerprint.startsWith("generic") ||
            fingerprint.contains("vbox") ||
            fingerprint.contains("test-keys") ||
            model.contains("google_sdk") ||
            model.contains("emulator") ||
            model.contains("android sdk built for") ||
            manufacturer.contains("genymotion") ||
            (brand.startsWith("generic") && device.startsWith("generic")) ||
            product.contains("sdk_gphone") ||
            product == "google_sdk"
    }

    private fun hasUnexpectedAppSignature(): Boolean {
        val expected = BuildConfig.EXPECTED_SIGNING_CERT_SHA256.trim()
        if (expected.isBlank()) return false

        val actualHashes: List<String> = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
                packageInfo.signingInfo
                    ?.apkContentsSigners
                    .orEmpty()
                    .map { it.toByteArray().sha256Hex() }
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES,
                ).signatures.orEmpty().map { it.toByteArray().sha256Hex() }
            }
        }.getOrDefault(emptyList())

        return actualHashes.none { it.equals(expected, ignoreCase = true) }
    }

    private fun ByteArray.sha256Hex(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(this)
            .joinToString(separator = "") { byte -> "%02x".format(Locale.US, byte) }

    companion object {
        private val ROOT_PATHS = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/bin/magisk",
            "/sbin/magisk",
        )
    }
}

data class DeviceIntegrityVerdict(
    val isTrustedForSensitiveActions: Boolean,
    val signals: Set<DeviceRiskSignal>,
    val playIntegrityState: PlayIntegrityState,
)

enum class DeviceRiskSignal {
    DEBUGGABLE_BUILD,
    DEBUGGER_ATTACHED,
    ROOT_SIGNALS,
    EMULATOR_SIGNALS,
    APK_SIGNATURE_MISMATCH,
}

enum class PlayIntegrityState {
    NOT_CONFIGURED,
    PASSED,
    FAILED,
}
