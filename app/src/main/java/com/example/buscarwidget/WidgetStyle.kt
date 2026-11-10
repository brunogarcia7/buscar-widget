package com.example.buscarwidget

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Estilos visuais disponíveis para a pílula do widget.
 *
 * Cada estilo aponta para um drawable de fundo, um ícone de lupa JÁ TINGIDO
 * (claro ou escuro) e a cor do texto. Usamos ícones pré-tingidos + text color
 * em vez de colorFilter reflexivo, porque RemoteViews.setInt("setColorFilter")
 * é frágil e pode lançar exceção em alguns aparelhos/versões.
 *
 * @property labelRes     nome exibido na tela de seleção
 * @property background   drawable de fundo da pílula
 * @property icon         lupa pré-tingida (claro/escuro)
 * @property contentColor cor do texto (0xAARRGGBB)
 */
enum class WidgetStyle(
    @StringRes val labelRes: Int,
    @DrawableRes val background: Int,
    @DrawableRes val icon: Int,
    @ColorInt val contentColor: Int
) {
    /** Vidro claro translúcido (padrão) — ótimo sobre papéis de parede variados. */
    GLASS_LIGHT(
        labelRes = R.string.style_glass_light,
        background = R.drawable.pill_glass_light,
        icon = R.drawable.ic_search_light,
        contentColor = 0xFFFFFFFF.toInt()
    ),

    /** Vidro escuro/fumê — texto branco sobre fundo escuro translúcido. */
    GLASS_DARK(
        labelRes = R.string.style_glass_dark,
        background = R.drawable.pill_glass_dark,
        icon = R.drawable.ic_search_light,
        contentColor = 0xFFFFFFFF.toInt()
    ),

    /** Vidro fosco (frosted) — mais opaco, simulando blur intenso. */
    FROSTED(
        labelRes = R.string.style_frosted,
        background = R.drawable.pill_frosted,
        icon = R.drawable.ic_search_dark,
        contentColor = 0xFF1C1C1E.toInt()
    ),

    /** Sólido estilo iOS claro — pílula branca quase opaca. */
    SOLID_LIGHT(
        labelRes = R.string.style_solid_light,
        background = R.drawable.pill_solid_light,
        icon = R.drawable.ic_search_dark,
        contentColor = 0xFF1C1C1E.toInt()
    );

    companion object {
        /** Ordem de exibição na tela de seleção. */
        fun ordered(): List<WidgetStyle> = entries.toList()
    }
}
