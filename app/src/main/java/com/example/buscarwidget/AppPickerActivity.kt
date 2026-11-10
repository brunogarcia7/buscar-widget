package com.example.buscarwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Tela de configuração (liquid glass). Permite escolher:
 *   1. o VISUAL do widget (estilos de vidro/fosco/sólido);
 *   2. o NAVEGADOR/app de busca que o widget abre (lista dinâmica via Intents).
 *
 * Visual: blur real forte no fundo (RenderEffect, API 31+), camada de
 * escurecimento para contraste, e animações de entrada/seleção elegantes.
 * Respeita "Reduzir movimento" do sistema (acessibilidade).
 */
class AppPickerActivity : AppCompatActivity() {

    private lateinit var styleRow: LinearLayout
    private var animationsEnabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        animationsEnabled = !isReduceMotionOn()

        applyBlur()
        setupStylePicker()
        setupAppList()
        if (animationsEnabled) runEntranceAnimations()
    }

    /** Lê a preferência de "Reduzir movimento" do sistema. */
    private fun isReduceMotionOn(): Boolean {
        val scale = Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        return scale == 0f
    }

    /** Blur real e forte no papel de parede (liquid glass), API 31+. */
    private fun applyBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById<View>(R.id.blur_backdrop).setRenderEffect(
                RenderEffect.createBlurEffect(90f, 90f, Shader.TileMode.CLAMP)
            )
        }
    }

    private fun runEntranceAnimations() {
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.fade_slide_down)
        findViewById<View>(R.id.title).startAnimation(slideDown)

        val headerStyle = AnimationUtils.loadAnimation(this, R.anim.fade_slide_down)
            .apply { startOffset = 80 }
        findViewById<View>(R.id.style_header).startAnimation(headerStyle)
        findViewById<View>(R.id.style_scroll).startAnimation(headerStyle)

        val headerApp = AnimationUtils.loadAnimation(this, R.anim.fade_slide_down)
            .apply { startOffset = 140 }
        findViewById<View>(R.id.app_header).startAnimation(headerApp)
    }

    // ---------- Seção 1: estilos do widget ----------

    private fun setupStylePicker() {
        styleRow = findViewById(R.id.style_row)
        val inflater = LayoutInflater.from(this)
        val current = SearchConfig.getWidgetStyle(this)

        WidgetStyle.ordered().forEachIndexed { index, style ->
            val chip = inflater.inflate(R.layout.item_style, styleRow, false)
            bindStyleChip(chip, style)
            chip.isSelected = (style == current)
            chip.setOnClickListener {
                SearchConfig.setWidgetStyle(this, style)
                updateChipSelection(style)
                popScale(it)
                refreshAllWidgets(this)
                Toast.makeText(this, R.string.style_saved, Toast.LENGTH_SHORT).show()
            }
            styleRow.addView(chip)

            if (animationsEnabled) {
                chip.alpha = 0f
                chip.translationY = 24f
                chip.animate()
                    .alpha(1f).translationY(0f)
                    .setStartDelay(120L + index * 60L)
                    .setDuration(420L)
                    .start()
            }
        }
    }

    private fun bindStyleChip(chip: View, style: WidgetStyle) {
        val pill = chip.findViewById<LinearLayout>(R.id.preview_pill)
        val icon = chip.findViewById<ImageView>(R.id.preview_icon)
        val label = chip.findViewById<TextView>(R.id.preview_label)
        val name = chip.findViewById<TextView>(R.id.style_name)

        pill.setBackgroundResource(style.background)
        icon.setColorFilter(style.contentColor, PorterDuff.Mode.SRC_IN)
        label.setTextColor(style.contentColor)
        name.setText(style.labelRes)
    }

    private fun updateChipSelection(selected: WidgetStyle) {
        val styles = WidgetStyle.ordered()
        for (i in 0 until styleRow.childCount) {
            styleRow.getChildAt(i).isSelected = (styles[i] == selected)
        }
    }

    /** Pequeno "pop" elástico ao selecionar (feedback de toque). */
    private fun popScale(view: View) {
        if (!animationsEnabled) return
        view.animate().cancel()
        view.scaleX = 0.92f
        view.scaleY = 0.92f
        view.animate()
            .scaleX(1f).scaleY(1f)
            .setInterpolator(OvershootInterpolator(3f))
            .setDuration(360L)
            .start()
    }

    // ---------- Seção 2: app de busca ----------

    private fun setupAppList() {
        val list = findViewById<RecyclerView>(R.id.app_list)
        val empty = findViewById<TextView>(R.id.empty_view)

        val apps = AppRepository.getSearchCapableApps(this)
        val current = SearchConfig.getTargetPackage(this)

        if (apps.isEmpty()) {
            list.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = AppListAdapter(apps, current) { entry ->
            SearchConfig.setTargetPackage(this, entry.packageName)
            refreshAllWidgets(this)
            Toast.makeText(
                this,
                getString(R.string.picker_saved, entry.label),
                Toast.LENGTH_SHORT
            ).show()
        }

        if (animationsEnabled) {
            list.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_stagger)
            list.scheduleLayoutAnimation()
        }
    }

    // ---------- comum ----------

    /** Pede ao sistema para reprocessar os widgets ativos (via broadcast). */
    private fun refreshAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, BuscarWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return

        val intent = Intent(context, BuscarWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
