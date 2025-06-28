package com.example.projectplanner.ui.graph

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.projectplanner.R
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(private val categories: List<CategoryData>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // Data class to hold category information
    data class CategoryData(
        val name: String,
        val amount: Float,
        val color: Int? = null
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvTotal: TextView = itemView.findViewById(R.id.tvCategoryTotal)
        val progressContainer: View = itemView.findViewById(R.id.progressContainer)
        val progressBar: View = itemView.findViewById(R.id.progressBar)
    }

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
        currency = java.util.Currency.getInstance("ZAR") // South African Rand
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categories[position]

        // Set category name
        holder.tvCategory.text = item.name

        // Format amount as currency
        holder.tvTotal.text = currencyFormat.format(item.amount)

        // Set category color if available
        item.color?.let { color ->
            holder.tvCategory.setTextColor(ContextCompat.getColor(holder.itemView.context, color))
            holder.progressBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, color))
        }

        // Handle progress visualization
        holder.progressContainer.post {
            val maxWidth = holder.progressContainer.width
            val progressWidth = (maxWidth * (item.amount / getMaxAmount())).toInt()

            // Update progress bar width
            val params = holder.progressBar.layoutParams
            params.width = progressWidth
            holder.progressBar.layoutParams = params
        }
    }

    private fun getMaxAmount(): Float {
        return categories.maxOfOrNull { it.amount } ?: 1000f
    }
}