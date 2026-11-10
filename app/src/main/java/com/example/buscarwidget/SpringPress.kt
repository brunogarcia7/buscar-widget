package com.example.buscarwidget

import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * Feedback de toque com FÍSICA DE MOLA (spring), reproduzindo a sensação do
 * iOS: ao pressionar, o elemento encolhe suavemente; ao soltar, volta com um
 * leve "quique" (bounce). Usa androidx.dynamicanimation.
 */
object SpringPress {

    private const val PRESSED_SCALE = 0.94f

    /**
     * Liga o efeito de mola em [view]. Mantém o comportamento de clique:
     * [onClick] é chamado quando o toque termina dentro da view.
     */
    fun attach(view: View, onClick: (View) -> Unit) {
        val scaleX = spring(view, SpringAnimation.SCALE_X)
        val scaleY = spring(view, SpringAnimation.SCALE_Y)

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    scaleX.animateToFinalPosition(PRESSED_SCALE)
                    scaleY.animateToFinalPosition(PRESSED_SCALE)
                }
                MotionEvent.ACTION_UP -> {
                    scaleX.animateToFinalPosition(1f)
                    scaleY.animateToFinalPosition(1f)
                    // Dispara o clique apenas se soltou dentro dos limites.
                    val inside = event.x >= 0 && event.y >= 0 &&
                        event.x <= v.width && event.y <= v.height
                    if (inside) {
                        v.performClick()
                        onClick(v)
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    scaleX.animateToFinalPosition(1f)
                    scaleY.animateToFinalPosition(1f)
                }
            }
            true
        }
    }

    private fun spring(view: View, property: androidx.dynamicanimation.animation.FloatPropertyCompat<View>) =
        SpringAnimation(view, property).apply {
            spring = SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
        }
}
