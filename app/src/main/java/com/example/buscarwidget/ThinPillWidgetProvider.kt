package com.example.buscarwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

/**
 * Pílula "Buscar" FINA — igual à pílula normal, mas mais baixa (achatada), para
 * ficar discreta e rente à dock. Usa o mesmo estilo/visual e o mesmo trampolim
 * de clique da pílula padrão.
 */
class ThinPillWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_buscar_thin)

            val style = SearchConfig.getWidgetStyle(context)
            views.setInt(R.id.widget_pill, "setBackgroundResource", style.background)
            views.setImageViewResource(R.id.widget_icon, style.icon)
            views.setTextColor(R.id.widget_label, style.contentColor)

            val trampoline = Intent(context, SearchTrampolineActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse("aurawidget://thin/$appWidgetId")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                trampoline,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_pill, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
