package com.example.projectplanner.ui.graph

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectplanner.R

class BadgeAdapter(private val badges: List<GraphFragment.Badge>) :
    RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBadge: ImageView = view.findViewById(R.id.ivBadge)
        val tvBadgeName: TextView = view.findViewById(R.id.tvBadgeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = badges.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val badge = badges[position]
        holder.tvBadgeName.text = badge.name
        holder.ivBadge.setImageResource(badge.iconRes)
    }  // Added the missing closing brace here
}  // Added the missing closing brace for the class