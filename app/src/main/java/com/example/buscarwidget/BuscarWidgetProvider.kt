package com.example.buscarwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Provider do widget "Buscar".
 *
 * O clique abre a [SearchTrampolineActivity] (lançamento de primeiro plano, sem
 * o bloqueio de Background Activity Launch do Android 10+), que então abre o
 * app escolhido direto na busca. O visual da pílula vem do [WidgetStyle].
 */
class BuscarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_buscar)

        // Estilo visual escolhido pelo usuário.
        val style = SearchConfig.getWidgetStyle(context)
        views.setInt(R.id.widget_pill, "setBackgroundResource", style.background)
        // Ícone JÁ TINGIDO (evita setColorFilter reflexivo, que é frágil).
        views.setImageViewResource(R.id.widget_icon, style.icon)
        views.setTextColor(R.id.widget_label, style.contentColor)

        // Clique -> Activity trampolim (lançamento de primeiro plano).
        val trampoline = Intent(context, SearchTrampolineActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            trampoline,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pill, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
