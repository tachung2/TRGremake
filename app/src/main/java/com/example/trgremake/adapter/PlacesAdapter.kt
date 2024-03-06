package com.example.trgremake.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trgremake.Place
import com.example.trgremake.R

class PlacesAdapter(private val places: List<Place>) : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.placeName)
        val addressTextView: TextView = view.findViewById(R.id.placeDescription)
        // 이미지뷰 등 다른 뷰들도 여기에 정의
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_place_pop_up_list_dialog_item, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.nameTextView.text = place.title
        holder.addressTextView.text = place.address
        // 이미지 로딩 로직도 추가
    }

    override fun getItemCount() = places.size
}