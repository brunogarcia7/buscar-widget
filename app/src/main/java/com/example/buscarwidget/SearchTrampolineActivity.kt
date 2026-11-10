package com.example.buscarwidget

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity "trampolim" invisível, acionada pelo clique no widget.
 *
 * Por que existe: lançar uma Activity a partir de um BroadcastReceiver é
 * BLOQUEADO em segundo plano no Android 10+ (Background Activity Launch), e o
 * clique no widget "morria" silenciosamente. Um clique de widget que abre uma
 * Activity é um lançamento de PRIMEIRO PLANO, então a partir daqui podemos
 * abrir o app de busca sem restrição.
 *
 * A Activity não tem UI: resolve o intent, abre o alvo com uma transição suave
 * estilo iOS (fade/scale) e se fecha na hora.
 */
class SearchTrampolineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchSearch()
        finish()
    }

    private fun launchSearch() {
        val targetPackage = SearchConfig.getTargetPackage(this)
        val searchIntent = AppRepository.buildSearchIntent(this, targetPackage)

        // Transição iOS-like: o app abre crescendo/fade em vez de deslizar.
        val options = ActivityOptions
            .makeCustomAnimation(this, R.anim.ios_activity_open, R.anim.ios_activity_close)
            .toBundle()

        if (searchIntent != null) {
            searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val ok = runCatching { startActivity(searchIntent, options) }.isSuccess
            if (ok) return
        }

        // Alvo ausente ou falhou: avisa e abre a tela de configuração.
        Toast.makeText(this, R.string.error_app_not_found, Toast.LENGTH_SHORT).show()
        runCatching {
            startActivity(
                Intent(this, AppPickerActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                options
            )
        }
    }
}
