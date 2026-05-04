package com.erivaldogelson.remedios.notifications

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.erivaldogelson.remedios.core.appContainer

class DoseLiveService : Service() {
    private val liveUpdateManager by lazy { applicationContext.appContainer.liveUpdateManager }
    private val handler = Handler(Looper.getMainLooper())
    private var activePayload: TreatmentLiveUpdatePayload? = null

    private val ticker = object : Runnable {
        override fun run() {
            val payload = activePayload ?: return
            if (payload.isComplete) {
                stopTreatment()
                return
            }
            promote(payload)
            handler.postDelayed(this, TICK_INTERVAL_MILLIS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        liveUpdateManager.ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_UPDATE -> {
                val payload = intent.getTreatmentLiveUpdatePayload() ?: return START_NOT_STICKY
                if (payload.isComplete) {
                    promote(payload)
                    stopTreatment()
                    return START_NOT_STICKY
                }
                activePayload = payload
                promote(payload)
                scheduleTick()
            }

            ACTION_STOP -> {
                val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
                if (medicationId < 0 || activePayload?.medicationId == medicationId) {
                    stopTreatment()
                }
            }

            else -> stopTreatment()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        super.onDestroy()
    }

    private fun promote(payload: TreatmentLiveUpdatePayload) {
        val notification: Notification = liveUpdateManager.buildTreatmentLiveNotification(payload)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun scheduleTick() {
        handler.removeCallbacks(ticker)
        handler.postDelayed(ticker, TICK_INTERVAL_MILLIS)
    }

    private fun stopTreatment() {
        handler.removeCallbacks(ticker)
        activePayload = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1001
        const val ACTION_START = "com.erivaldogelson.remedios.action.START_TREATMENT_LIVE"
        const val ACTION_UPDATE = "com.erivaldogelson.remedios.action.UPDATE_TREATMENT_LIVE"
        const val ACTION_STOP = "com.erivaldogelson.remedios.action.STOP_TREATMENT_LIVE"
        const val EXTRA_MEDICATION_ID = "extra_treatment_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_treatment_medication_name"
        const val EXTRA_DOSAGE = "extra_treatment_dosage"
        const val EXTRA_DOSES_TAKEN = "extra_treatment_doses_taken"
        const val EXTRA_TOTAL_DOSES = "extra_treatment_total_doses"
        const val EXTRA_NEXT_DOSE_AT = "extra_treatment_next_dose_at"
        const val EXTRA_ACCENT_COLOR = "extra_treatment_accent_color"
        private const val TICK_INTERVAL_MILLIS = 60_000L

        fun intent(
            context: Context,
            action: String,
            payload: TreatmentLiveUpdatePayload,
        ): Intent = Intent(context, DoseLiveService::class.java).apply {
            this.action = action
            putTreatmentLiveUpdatePayload(payload)
        }

        fun stopIntent(context: Context, medicationId: Long = -1L): Intent =
            Intent(context, DoseLiveService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_MEDICATION_ID, medicationId)
            }
    }
}
