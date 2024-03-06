package com.example.trgremake.adapter

import PlaceCardUpFragment
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trgremake.R
import com.example.trgremake.RouteActivity

class TitlesAdapter(private val titles: MutableList<String>) : RecyclerView.Adapter<TitlesAdapter.TitleViewHolder>() {

    class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)

        fun bind(title: String) {
            textViewTitle.text = title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TitleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_title, parent, false)
        return TitleViewHolder(view)
    }

    override fun onBindViewHolder(holder: TitleViewHolder, position: Int) {
        val title = titles[position]

        holder.bind(title)

        holder.itemView.setOnClickListener {
            // 클릭 시 RouteActivity 시작
            val intent = Intent(holder.itemView.context, RouteActivity::class.java).apply {
                putExtra("SEARCH_QUERY", title)
            }
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = titles.size

    fun updateData(newTitles: List<String>) {
        titles.clear()
        titles.addAll(newTitles)
        notifyDataSetChanged()
    }
}