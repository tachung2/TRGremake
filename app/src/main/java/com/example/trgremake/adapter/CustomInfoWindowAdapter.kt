package com.example.trgremake.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.trgremake.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View {
        // 사용자 정의 레이아웃을 반환합니다.
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        val title = view.findViewById<TextView>(R.id.custom_info_title)
        val button = view.findViewById<Button>(R.id.custom_btn)
        title.text = marker.title
        // 버튼 클릭 이벤트 설정
        button.setOnClickListener {
            // 버튼 클릭 시 수행할 작업
            Toast.makeText(context, "버튼 클릭됨", Toast.LENGTH_SHORT).show()
        }
        return view
    }

    override fun getInfoContents(marker: Marker): View? {
        // 기본 프레임을 사용하고, 사용자 정의 뷰를 채웁니다.
        return null
    }
}