package com.example.buscarwidget

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build

/**
 * Representa um app instalado que pode ser escolhido pelo usuário.
 */
data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: Drawable?
)

/**
 * Descobre, via Intents do Android, os NAVEGADORES instalados (+ o Pixel
 * Search) e sabe abrir cada um DIRETO NO CAMPO DE BUSCA.
 *
 * Por que só navegadores + Pixel Search?
 * A ação genérica ACTION_SEARCH puxava apps que não servem para busca
 * (Contatos, Drive, Gmail, Play Services, shims do sistema...). Para uma lista
 * limpa e previsível, filtramos por navegadores reais — apps que atendem
 * ACTION_VIEW de uma URL https com a categoria BROWSABLE — e incluímos o Pixel
 * Search por pacote.
 */
object AppRepository {

    /**
     * Apps de busca "especiais" (não são navegadores BROWSABLE, mas fazem
     * pesquisa). Incluídos por pacote sempre que estiverem instalados.
     */
    private val EXTRA_SEARCH_PACKAGES = listOf(
        "com.google.android.googlequicksearchbox", // app Google (nativo)
        "rk.android.app.pixelsearch"               // Pixel Search
    )

    fun getSearchCapableApps(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val resolved = LinkedHashMap<String, ResolveInfo>()

        // 1. Navegadores reais: ACTION_VIEW de https + categoria BROWSABLE.
        val browserProbe = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        for (info in queryActivities(pm, browserProbe)) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg == context.packageName) continue
            resolved.putIfAbsent(pkg, info)
        }

        // 2. Apps de busca especiais (Pixel Search) por pacote, se instalados.
        for (pkg in EXTRA_SEARCH_PACKAGES) {
            if (resolved.containsKey(pkg)) continue
            val launch = pm.getLaunchIntentForPackage(pkg) ?: continue
            val match = queryActivities(pm, launch).firstOrNull() ?: continue
            resolved.putIfAbsent(pkg, match)
        }

        return resolved.values
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                AppEntry(
                    label = info.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = runCatching { info.loadIcon(pm) }.getOrNull()
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    /**
     * Monta o melhor Intent para abrir o app DIRETO NA BUSCA.
     *
     * Ordem de tentativa (do mais específico ao mais genérico):
     *   1. ACTION_WEB_SEARCH direcionado ao pacote, com query vazia — a maioria
     *      dos navegadores (Chrome, Brave, Firefox...) abre focando o campo de
     *      busca/endereço quando recebe esse intent.
     *   2. ACTION_SEARCH direcionado ao pacote — busca interna (Pixel Search).
     *   3. getLaunchIntentForPackage — abre o app normalmente (fallback).
     *
     * Retorna null se o pacote não estiver instalado/visível.
     */
    fun buildSearchIntent(context: Context, packageName: String): Intent? {
        val pm = context.packageManager

        // 1. Busca web focada no app escolhido (campo de busca em foco).
        //    Cobre navegadores (Chrome, Brave, Firefox...) e o app Google.
        //    query vazia = abre a tela de busca sem disparar resultados.
        val webSearch = Intent(Intent.ACTION_WEB_SEARCH).apply {
            setPackage(packageName)
            putExtra(SearchManager.QUERY, "")
        }
        if (queryActivities(pm, webSearch).isNotEmpty()) {
            return webSearch.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        }

        // 2. Busca interna do app (ex.: Pixel Search abre já na barra).
        val appSearch = Intent(Intent.ACTION_SEARCH).apply { setPackage(packageName) }
        resolveExplicit(pm, appSearch)?.let { return it }

        // 3. Assistente/busca por voz-texto (fallback para o app Google).
        val assist = Intent(Intent.ACTION_ASSIST).apply { setPackage(packageName) }
        resolveExplicit(pm, assist)?.let { return it }

        // 4. Último recurso: só abre o app.
        return pm.getLaunchIntentForPackage(packageName)
    }

    /** Converte uma intent com setPackage em intent EXPLÍCITA (classe exata). */
    private fun resolveExplicit(pm: PackageManager, intent: Intent): Intent? {
        val match = queryActivities(pm, intent).firstOrNull() ?: return null
        val activityInfo = match.activityInfo ?: return null
        return Intent(intent.action).apply {
            setClassName(activityInfo.packageName, activityInfo.name)
        }
    }

    private fun queryActivities(pm: PackageManager, intent: Intent): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
    }
}
