package com.example.trgremake.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.trgremake.Place
import com.example.trgremake.R

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.nio.charset.Charset

class RouteAdapter(
    private val context: Context,
    var routeNames: MutableList<String>,
    private val onItemAdded: (Int) -> Unit, // 콜백을 통해 아이템이 추가된 인덱스를 액티비티로 전달
    private val onItemsUpdated: (Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ADD = 0
    private val TYPE_ROUTE = 1
    val routePlaceIds = mutableMapOf<String, MutableList<Int>>()
    private var selectedRouteIndex = -1
    var routeIds = mutableMapOf<String, Long>()

    var items = routeNames.toMutableList().apply { add("Add") }


    override fun getItemCount() = items.size

    fun setRouteId(routeName: String, routeId: Long) {
        routeIds[routeName] = routeId
    }

    fun getSelectedRouteId(): Long? {
        val selectedRouteName = getSelectedRouteName()
        return routeIds[selectedRouteName]
    }

    fun addPlaceIdToRoute(routeName: String, placeId: Long) {
        val placeIdList = routePlaceIds.getOrPut(routeName) { mutableListOf() }
        if (placeIdList.size >= 6) {
            Toast.makeText(context, "이 루트에는 최대 6개의 여행지만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
        } else {
            val place = placeId.toInt()
            placeIdList.add(place)
            // 이 부분에 쿼리로 /place 엔드포인트에 get요청하고 받아온 데이터 중에 이름을 토스트로 저장되었다고 할 거야
            val requestQueue = Volley.newRequestQueue(context)
            val url = "https://belleravi.co.kr/place/$place"

            val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    val correctEncodingResponse = String(response.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
                    val places = JSONObject(correctEncodingResponse)

                    if (places.has("title")) {
                        val title = places.getString("title")
                        Toast.makeText(context, "$title 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // "title" 키가 없는 경우 처리
                        Toast.makeText(context, "장소 제목이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    // 에러 처리 로직
                    Toast.makeText(context, "오류: ${error.message}", Toast.LENGTH_SHORT).show()
                })

            requestQueue.add(stringRequest)

        }
    }

    private fun parseJsonToPlaces(jsonString: String): List<Place> {
        val gson = Gson()
        val type = object : TypeToken<List<Place>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun getPlaceIdsForRoute(routeName: String): List<Int> {
        return routePlaceIds[routeName] ?: emptyList()
    }

    fun updateItems() {
        items = routeNames.toMutableList().apply { add("Add") }
        notifyDataSetChanged()
        onItemsUpdated(routeNames.isNotEmpty())

    }

    fun getSelectedRouteName(): String {
        return if (selectedRouteIndex >= 0 && selectedRouteIndex < routeNames.size) {
            routeNames[selectedRouteIndex]
        } else {
            "" // 선택된 루트가 없는 경우
        }
    }

    fun updateData(newRouteNames: List<String>) {
        // 기존 루트 이름 목록을 새 목록으로 교체
        routeNames.clear()
        routeNames.addAll(newRouteNames)

        // 'Add' 항목을 끝에 다시 추가
        items.clear()
        items.addAll(newRouteNames)
        items.add("Add")

        // 어댑터에 변경 사항을 알림
        notifyDataSetChanged()

        // 리스트가 비어있지 않은지 콜백을 통해 액티비티에 알림
        onItemsUpdated(routeNames.isNotEmpty())
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] == "Add") TYPE_ADD else TYPE_ROUTE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_add_button, parent, false)
            AddViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_route, parent, false)
            RouteViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RouteViewHolder) {
            holder.bind(routeNames[position], position)
        } else if (holder is AddViewHolder) {
            holder.itemView.setOnClickListener {
                showAddRouteDialog()
            }
        }
    }


    private fun showAddRouteDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_route, null)
        val editText = dialogView.findViewById<EditText>(R.id.editRouteName)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                val routeName = editText.text.toString().trim()
                if (routeName.isEmpty()) {
                    // 루트 이름이 비어 있는 경우, 경고 메시지를 표시
                    Toast.makeText(context, "루트 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (routeNames.contains(routeName)) {
                    // 루트 이름이 중복되는 경우, 경고 메시지를 표시
                    Toast.makeText(context, "중복되는 루트 이름이 있습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 새로운 루트 이름을 추가
                    val newIndex = routeNames.size
                    routeNames.add(routeName)
                    items.add(newIndex, routeName)
                    notifyItemInserted(newIndex)
                    onItemAdded(newIndex)
                    updateItems()
                }
            }
            .setNegativeButton("취소", null)
            .show()

        dialog.window?.apply {
            // 배경을 둥근 모서리로 설정
            setBackgroundDrawableResource(R.drawable.rounded_corners)
            // 다이얼로그의 가로 길이를 설정 (예: 화면의 80%)
            val width = (context.resources.displayMetrics.widthPixels * 0.60).toInt()
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }


    }

    fun removePlaceIdFromRoute(routeName: String, placeId: Int) {
        routePlaceIds[routeName]?.let { places ->
            places.remove(placeId)
            if (places.isEmpty()) {
                routePlaceIds.remove(routeName)
            }
            notifyDataSetChanged()
        }
    }

    fun fetchPlaceDetails(placeId: Int, onResult: (String) -> Unit) {
        val url = "https://belleravi.co.kr/place/$placeId"
        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val correctEncodingResponse = String(response.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
                val placeObject = JSONObject(correctEncodingResponse)
                val placeName = placeObject.getString("title")
                onResult(placeName)
            },
            { error ->
                onResult("장소 정보를 가져오는데 실패함")
            })

        requestQueue.add(stringRequest)
    }


    // 어댑터 내에 루트 이름을 추가하고 리사이클러 뷰를 업데이트하는 함수

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(routeName: String, position: Int) {
            val toggleButton = itemView.findViewById<ToggleButton>(R.id.btnRoute)
            toggleButton.textOn = routeName
            toggleButton.textOff = routeName

            // 기존 리스너를 제거하고 상태를 재설정
            toggleButton.setOnCheckedChangeListener(null)
            toggleButton.isChecked = position == selectedRouteIndex

            // 새로운 리스너 설정
            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (selectedRouteIndex != -1 && selectedRouteIndex != position) {
                        notifyItemChanged(selectedRouteIndex)
                    }
                    selectedRouteIndex = position
                } else if (selectedRouteIndex == position) {
                    selectedRouteIndex = -1
                }
            }
        }
    }

    inner class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // + 버튼을 위한 뷰 홀더입니다.
        init {
            itemView.findViewById<Button>(R.id.plus_btn).setOnClickListener {
                showAddRouteDialog()
            }
        }
    }
}