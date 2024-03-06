package com.example.trgremake.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trgremake.Place
import com.example.trgremake.R
import com.example.trgremake.RouteActivity


class RecoPGAdapter(private val places: List<Place>, private val context: Context) :
    RecyclerView.Adapter<RecoPGAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.rv_place_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.rv_place_description)
        private val imageView: ImageView = itemView.findViewById(R.id.img_reco)

        fun bind(place: Place, context: Context) {
            nameTextView.text = place.title
            descriptionTextView.text = place.address
            Glide.with(itemView.context)
                .load(place.image)
                .into(imageView)

            itemView.setOnClickListener {
                // 클릭 시 로그 출력
                Log.d("RecoRVAdapter", "Clicked on item: ${place.title}")

                // 클릭 시 RouteActivity로 이동하며 'query'로 place.title 전달
                val intent = Intent(context, RouteActivity::class.java).apply {
                    putExtra("SEARCH_QUERY", place.title)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reco_list_main_rv, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(places[position], context)
    }

    override fun getItemCount() = places.size
}