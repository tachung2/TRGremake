package com.example.trgremake

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.trgremake.adapter.RecoPGAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecoActivity : AppCompatActivity() {

    private lateinit var mainImage: ImageView
    private lateinit var titleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommend)

        val fabMap = findViewById<ImageView>(R.id.fab_map)

        mainImage = findViewById(R.id.main_image)
        titleText = findViewById(R.id.title_text)


        // MainActivity에서 넘어온 쿼리를 가져옴
        val query = intent.getStringExtra("query")
        query?.let {
            sendSearchQuery(query)
        }

        fabMap.setOnClickListener {
            val mapIntent = Intent(this, RouteActivity::class.java)
            mapIntent.putExtra("SEARCH_QUERY", query)
            startActivity(mapIntent)
        }
    }

    private fun sendSearchQuery(query: String) {
        val searchUrl = "https://belleravi.co.kr/search?query=$query"

        // Volley 요청 큐
        val requestQueue = Volley.newRequestQueue(this)

        // StringRequest 생성 및 요청
        val stringRequest = StringRequest(Request.Method.GET, searchUrl, { response ->
            // 서버로부터 응답을 받았을 때 처리
            val correctEncodingResponse =
                String(response.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
            val places = parseJsonToPlaces(correctEncodingResponse)
            if (places.isNotEmpty()) {
                // 첫 번째 결과 사용
                val firstPlace = places[0]
                updateUI(firstPlace.title, firstPlace.image)
                displayPlaces(places)
            }

        }, { error ->
            // 에러 발생 시 처리
            error.printStackTrace()
        })

        requestQueue.add(stringRequest)
    }

    private fun updateUI(title: String, imageUrl: String) {
        titleText.text = title
        Glide.with(this)
            .load(imageUrl)
            .into(mainImage)
    }

    private fun parseJsonToPlaces(jsonString: String): List<Place> {
        val gson = Gson()
        val type = object : TypeToken<List<Place>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    private fun displayPlaces(places: List<Place>) {
        // 첫 번째 장소의 데이터를 메인 이미지와 텍스트에 설정
        if (places.isNotEmpty()) {
            // RecyclerView 설정
            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = RecoPGAdapter(places, this)
        } else {
            Toast.makeText(this, "No places found", Toast.LENGTH_SHORT).show()
        }
    }


}