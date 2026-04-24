package com.erivaldogelson.remedios.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.erivaldogelson.remedios.MainActivity
import com.erivaldogelson.remedios.R
import com.erivaldogelson.remedios.core.appContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.format.DateTimeFormatter

abstract class MedicationWidgetProvider(
    private val layoutId: Int,
) : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId, layoutId)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            listOf(
                SmallMedicationWidgetProvider::class.java,
                WideMedicationWidgetProvider::class.java,
            ).forEach { providerClass ->
                val ids = manager.getAppWidgetIds(ComponentName(context, providerClass))
                val layout = if (providerClass == SmallMedicationWidgetProvider::class.java) {
                    R.layout.widget_medication_2x2
                } else {
                    R.layout.widget_medication_4x2
                }
                ids.forEach { updateWidget(context, manager, it, layout) }
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            layoutId: Int,
        ) {
            val snapshot = runBlocking {
                context.appContainer.medicationRepository.observeDashboard().first()
            }
            val views = RemoteViews(context.packageName, layoutId)
            val nextDose = snapshot.nextDose
            if (nextDose == null) {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_empty_title))
                views.setTextViewText(R.id.widget_subtitle, context.getString(R.string.widget_empty_subtitle))
                views.setTextViewText(R.id.widget_time, "--")
                views.setTextViewText(R.id.widget_meta, context.getString(R.string.widget_empty_meta))
            } else {
                val minutes = nextDose.remainingMinutes.coerceAtLeast(0)
                views.setTextViewText(R.id.widget_title, nextDose.medicationName)
                views.setTextViewText(R.id.widget_subtitle, nextDose.dosage)
                views.setTextViewText(R.id.widget_time, nextDose.scheduledAt.format(DateTimeFormatter.ofPattern("HH:mm")))
                views.setTextViewText(
                    R.id.widget_meta,
                    context.resources.getQuantityString(R.plurals.widget_minutes_remaining, minutes.toInt(), minutes),
                )
            }
            views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context))
            manager.updateAppWidget(widgetId, views)
        }

        private fun launchIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                2100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

class SmallMedicationWidgetProvider : MedicationWidgetProvider(R.layout.widget_medication_2x2)

class WideMedicationWidgetProvider : MedicationWidgetProvider(R.layout.widget_medication_4x2)
