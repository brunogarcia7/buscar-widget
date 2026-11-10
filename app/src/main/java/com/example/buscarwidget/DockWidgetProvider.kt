package com.example.buscarwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/**
 * Provider do segundo widget: a DOCK DE VIDRO decorativa (estilo iOS).
 *
 * É puramente visual — não tem clique nem ação. Serve para o usuário posicionar
 * a faixa de vidro atrás dos ícones de apps na tela inicial.
 */
class DockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_dock)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
