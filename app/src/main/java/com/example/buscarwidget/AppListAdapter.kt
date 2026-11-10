package com.example.buscarwidget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter da lista de apps compatíveis. Destaca o app selecionado com uma
 * marca animada e dá feedback elástico ao toque.
 */
class AppListAdapter(
    private val items: List<AppEntry>,
    private var selectedPackage: String?,
    private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
        val selected: ImageView = view.findViewById(R.id.app_selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.label
        holder.icon.setImageDrawable(item.icon)

        val isSelected = item.packageName == selectedPackage
        holder.selected.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        holder.selected.scaleX = if (isSelected) 1f else 0f
        holder.selected.scaleY = if (isSelected) 1f else 0f

        // Feedback de toque com física de mola (iOS).
        SpringPress.attach(holder.itemView) {
            updateSelection(item.packageName)
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    /** Cancela animações pendentes ao reciclar para evitar flicker. */
    override fun onViewRecycled(holder: AppViewHolder) {
        holder.itemView.animate().cancel()
        holder.selected.animate().cancel()
        holder.itemView.scaleX = 1f
        holder.itemView.scaleY = 1f
        super.onViewRecycled(holder)
    }

    /** Atualiza a marca de selecionado com animação de surgimento. */
    private fun updateSelection(packageName: String) {
        val previous = items.indexOfFirst { it.packageName == selectedPackage }
        selectedPackage = packageName
        val current = items.indexOfFirst { it.packageName == packageName }
        if (previous >= 0) notifyItemChanged(previous)
        if (current >= 0) notifyItemChanged(current, PAYLOAD_ANIMATE_CHECK)
    }

    override fun onBindViewHolder(
        holder: AppViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_ANIMATE_CHECK)) {
            // Anima só a marca de seleção, sem rebind completo.
            holder.selected.visibility = View.VISIBLE
            holder.selected.scaleX = 0f
            holder.selected.scaleY = 0f
            holder.selected.animate()
                .scaleX(1f).scaleY(1f)
                .setInterpolator(OvershootInterpolator(3f))
                .setDuration(360L)
                .start()
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    companion object {
        private const val PAYLOAD_ANIMATE_CHECK = "animate_check"
    }
}
