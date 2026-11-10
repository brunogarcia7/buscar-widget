package com.example.buscarwidget

import android.content.Context

/**
 * Configuração central do widget de busca.
 *
 * Agora o app-alvo é DINÂMICO: o usuário escolhe na tela de configuração
 * ([AppPickerActivity]) e a escolha fica salva em SharedPreferences. Caso o
 * usuário ainda não tenha escolhido nada, usamos [DEFAULT_PACKAGE] como padrão.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  >>> PADRÃO DE FÁBRICA <<<                                             │
 * │  Para mudar o app aberto "de fábrica" (antes de o usuário escolher),  │
 * │  altere DEFAULT_PACKAGE. O usuário sempre pode sobrescrever pela tela │
 * │  de configuração.                                                     │
 * └─────────────────────────────────────────────────────────────────────┘
 */
object SearchConfig {

    /** Pacote usado enquanto o usuário não escolheu nada. */
    const val DEFAULT_PACKAGE: String = "rk.android.app.pixelsearch"

    private const val PREFS_NAME = "buscar_widget_prefs"
    private const val KEY_TARGET_PACKAGE = "target_package"
    private const val KEY_WIDGET_STYLE = "widget_style"

    /**
     * Retorna o pacote do app que o widget deve abrir: o escolhido pelo
     * usuário ou, na ausência dele, o [DEFAULT_PACKAGE].
     */
    fun getTargetPackage(context: Context): String {
        return prefs(context).getString(KEY_TARGET_PACKAGE, DEFAULT_PACKAGE)
            ?: DEFAULT_PACKAGE
    }

    /** Salva o pacote escolhido pelo usuário. */
    fun setTargetPackage(context: Context, packageName: String) {
        prefs(context).edit()
            .putString(KEY_TARGET_PACKAGE, packageName)
            .apply()
    }

    /** Estilo visual do widget escolhido pelo usuário. */
    fun getWidgetStyle(context: Context): WidgetStyle {
        val name = prefs(context).getString(KEY_WIDGET_STYLE, WidgetStyle.GLASS_LIGHT.name)
        return runCatching { WidgetStyle.valueOf(name!!) }.getOrDefault(WidgetStyle.GLASS_LIGHT)
    }

    /** Salva o estilo visual escolhido. */
    fun setWidgetStyle(context: Context, style: WidgetStyle) {
        prefs(context).edit().putString(KEY_WIDGET_STYLE, style.name).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
