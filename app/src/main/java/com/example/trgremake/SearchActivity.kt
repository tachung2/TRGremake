package com.example.trgremake

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.trgremake.adapter.RouteAdapter
import com.example.trgremake.adapter.TitlesAdapter
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

data class TourPlace(
    val id: Int,
    val title: String,
    val address: String,
    val image: String,
    val mapx: Double,
    val mapy: Double
)
class SearchActivity : AppCompatActivity() {
    private val titlesList = mutableListOf<String>()
    private lateinit var adapter: TitlesAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search)

        recyclerView = findViewById(R.id.recycler_view)
        adapter = TitlesAdapter(titlesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupTag()

        val searchView: SearchView = findViewById(R.id.search)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 사용자가 검색 버튼을 눌렀을 때의 처리
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 텍스트가 변경될 때마다 호출
                if (newText != null && newText.length >= 2) {
                    // 두 글자 이상 입력되었을 때 검색 처리
                    performSearch(newText)
                }
                return true
            }
        })
    }

    private fun setupTag() {
        val p1: TextView = findViewById(R.id.p1)
        val p2: TextView = findViewById(R.id.p2)
        val p3: TextView = findViewById(R.id.p3)
        val p4: TextView = findViewById(R.id.p4)
        val p5: TextView = findViewById(R.id.p5)
        val p6: TextView = findViewById(R.id.p6)
        val p7: TextView = findViewById(R.id.p7)
        val p8: TextView = findViewById(R.id.p8)

        p1.setOnClickListener { tagSearch("홍대") }
        p2.setOnClickListener { tagSearch("춘천") }
        p3.setOnClickListener { tagSearch("경주") }
        p4.setOnClickListener { tagSearch("나주") }
        p5.setOnClickListener { tagSearch("여수") }
        p6.setOnClickListener { tagSearch("강릉") }
        p7.setOnClickListener { tagSearch("순천") }
        p8.setOnClickListener { tagSearch("인천") }
    }

    fun tagSearch(query: String) {
        // 검색 로직을 구현합니다.
        // 예를 들어 검색 쿼리를 사용하여 서버에 검색을 요청하거나,
        // 검색 결과를 표시하는 액티비티로 이동할 수 있습니다.
        val intent = Intent(this, RouteActivity::class.java).apply {
            putExtra("SEARCH_QUERY", query)
        }
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.remain_still, R.anim.slide_down)
    }

    private fun performSearch(query: String) {
        val url = "https://belleravi.co.kr/search?query=$query"

        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val correctEncodingResponse = String(response.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
                val jsonArray = JSONArray(correctEncodingResponse)
                val newTitles = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    newTitles.add(item.getString("title"))
                }
                adapter.updateData(newTitles)
            },
            {
                // 오류 처리...
            })

        queue.add(stringRequest)

    }
}