package com.example.trgremake

import PlaceCardUpFragment
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.trgremake.adapter.CustomInfoWindowAdapter
import com.example.trgremake.adapter.RouteAdapter
import com.example.trgremake.adapter.TitlesAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

data class Route(
    val id: Long,
    val routeName: String,
    val p1: Long,
    val p2: Long,
    val p3: Long,
    val p4: Long,
    val p5: Long,
    val p6: Long,

)

data class Place(
    val id: Long,
    val title: String,
    val address: String,
    val image: String,
    val mapx: Double,
    val mapy: Double
    // 필요한 경우 여기에 더 많은 필드를 추가할 수 있습니다.
)
class RouteActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var placeCardFragment: PlaceCardUpFragment
    private lateinit var routeAdapter: RouteAdapter
    private var currentMarker: Marker? = null
    private var routePlaces: List<Place> = listOf()


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.route)

        val routeLayout = findViewById<DrawerLayout>(R.id.route_layout)
        val routeMenuImageView = findViewById<ImageView>(R.id.route_menu)
        val token = getSavedToken()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        routeMenuImageView.setOnClickListener {
            val selectedRouteName = routeAdapter.getSelectedRouteName()
            if (selectedRouteName.isNotEmpty()) {
                routeLayout.openDrawer(GravityCompat.START)
                updateDrawerMenu(selectedRouteName)
            } else {
                Toast.makeText(this, "선택된 루트가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        setupMapFragment()
        setupBottomSheet()
        setupLocationClient()
        setupRecyclerView()
        setupSaveRouteButton()
        setupGuideButton()
        fetchUserId(token) { userId ->
            loadRouteData(userId)
        }

        if (savedInstanceState == null) {
            placeCardFragment = PlaceCardUpFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.placeCardContainer, placeCardFragment)
                .commit()
        }

//        검색 부분
        val searchEditText: EditText = findViewById(R.id.Search)
        val searchImageView: ImageView = findViewById(R.id.search_image)

        searchImageView.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                sendSearchQuery(query)
            } else {
                // 검색 쿼리가 비어있을 때의 처리 (예: 사용자에게 입력 요청 메시지 표시)
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // PlaceCardFragment 인스턴스 초기화 및 트랜잭션 처리
        if (savedInstanceState == null) {
            placeCardFragment = PlaceCardUpFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.placeCardContainer, placeCardFragment)
                .commit()
        }
    }

    private fun setupGuideButton() {
        val guideButton: ToggleButton = findViewById(R.id.guide_btn)
        guideButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                drawRouteOnMap()
            } else {
                clearRouteFromMap()
            }
        }
    }

    private fun clearRouteFromMap() {
        mMap.clear() // 지도의 모든 마커와 폴리라인을 제거
        // 필요한 경우 기타 초기화 로직 추가
    }

    private fun drawRouteOnMap() {
        val selectedRouteName = routeAdapter.getSelectedRouteName()
        if (selectedRouteName.isEmpty()) {
            Toast.makeText(this, "먼저 루트를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val placeIds = routeAdapter.getPlaceIdsForRoute(selectedRouteName)
        fetchPlacesDetails(placeIds) { places ->
            this.routePlaces = places

            // 지도에 마커와 라인 그리기
            if (routePlaces.isNotEmpty()) {
                // 지도에 마커와 라인 그리기
                val polylineOptions = PolylineOptions()
                    .color(Color.parseColor("#308FFA"))
                    .width(10f)

                routePlaces.forEachIndexed { index, place ->
                    val latLng = LatLng(place.mapy, place.mapx)
                    val markerTitle = "${index + 1}. ${place.title}"
                    mMap.addMarker(MarkerOptions().position(latLng).title(markerTitle))
                    polylineOptions.add(latLng)
                }

                mMap.addPolyline(polylineOptions)

                // 첫 번째 장소의 위치로 지도 카메라 이동
                val firstPlace = routePlaces.first()
                val cameraPosition = LatLng(firstPlace.mapy, firstPlace.mapx)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition, 15f))
            } else {
                Toast.makeText(this, "루트에 장소가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchPlacesDetails(placeIds: List<Int>, onResult: (List<Place>) -> Unit) {
        val places = mutableListOf<Place>()
        val requestQueue = Volley.newRequestQueue(this)

        placeIds.forEach { placeId ->
            val url = "https://belleravi.co.kr/place/$placeId"
            val stringRequest = StringRequest(Request.Method.GET, url, { response ->
                val correctEncodingResponse = String(response.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
                val placeObject = JSONObject(correctEncodingResponse)
                val place = Place(
                    id = placeObject.getLong("id"),
                    title = placeObject.getString("title"),
                    address = placeObject.getString("address"),
                    image = placeObject.getString("image"),
                    mapx = placeObject.getDouble("mapx"),
                    mapy = placeObject.getDouble("mapy")
                )
                places.add(place)

                if (places.size == placeIds.size) {
                    onResult(places)
                }
            }, { error ->
                Log.e("Error", "장소 정보를 가져오는데 실패: ${error.message}")
            })

            requestQueue.add(stringRequest)
        }
    }

    private fun deleteSelectedRoute() {
        // 현재 선택된 루트의 이름을 얻는다
        val selectedRouteName = routeAdapter.getSelectedRouteName()
        val routeId = routeAdapter.routeIds[selectedRouteName]

        if (routeId == null) {
            Toast.makeText(this, "선택된 루트가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://belleravi.co.kr/delete/$routeId"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.DELETE, url,
            { response ->
                Toast.makeText(this, "루트가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                updateAfterRouteDeletion(selectedRouteName)
                val drawerLayout = findViewById<DrawerLayout>(R.id.route_layout)
                drawerLayout.closeDrawer(GravityCompat.START) // 사이드바 닫기
            },
            { error ->
                Toast.makeText(this, "루트 삭제 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(stringRequest)
    }

    private fun updateAfterRouteDeletion(deletedRouteName: String) {
        // 어댑터의 데이터에서 삭제된 루트 제거
        routeAdapter.routeNames.remove(deletedRouteName)
        routeAdapter.routeIds.remove(deletedRouteName)
        routeAdapter.routePlaceIds.remove(deletedRouteName)
        routeAdapter.notifyDataSetChanged()
        updateDrawerMenu("")
    }

    private fun updateDrawerMenu(routeName: String) {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.getHeaderView(0)?.let {
            navigationView.removeHeaderView(it)
        }
        val menu = navigationView.menu
        menu.clear() // 이전 아이템들을 모두 제거

        val headerView = navigationView.inflateHeaderView(R.layout.nav_drawer_header)
        val routeNameTextView = headerView.findViewById<TextView>(R.id.navDrawerHeader)
        val deleteButton = headerView.findViewById<Button>(R.id.deleteRouteButton)

        routeNameTextView.text = routeName

        deleteButton.setOnClickListener {
            deleteSelectedRoute()
        }


        // 루트에 대한 여행지 ID 목록을 가져옴
        val placeIds = routeAdapter.getPlaceIdsForRoute(routeName)
        placeIds.forEach { placeId ->
            routeAdapter.fetchPlaceDetails(placeId) { placeName ->
                runOnUiThread {
                    val itemView = layoutInflater.inflate(R.layout.nav_drawer_item, null)
                    val textView = itemView.findViewById<TextView>(R.id.navDrawerItemText)
                    val deleteItemButton = itemView.findViewById<ImageButton>(R.id.navDrawerItemDeleteButton)

                    textView.text = placeName
                    deleteItemButton.setOnClickListener {
                        routeAdapter.removePlaceIdFromRoute(routeName, placeId)
                        updateDrawerMenu(routeName) // 메뉴 다시 갱신
                    }

                    val menuItem = menu.add(Menu.NONE, placeId, Menu.NONE, "")
                    menuItem.actionView = itemView
                }
            }
        }
    }

    private fun loadRouteData(userId: Long) {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "https://belleravi.co.kr/route?userId=$userId"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                val routeNames = mutableListOf<String>()
                for (i in 0 until response.length()) {
                    try {
                        val routeObject = response.getJSONObject(i)
                        val routeName = routeObject.getString("routename")
                        val routeId = routeObject.getLong("id")
                        routeNames.add(routeName)

                        // 루트 ID를 맵에 추가
                        routeAdapter.setRouteId(routeName, routeId)

                        val placeIds = mutableListOf<Int>()
                        for (j in 1..6) {
                            val placeId = routeObject.optInt("p$j")
                            if (placeId != 0) {
                                placeIds.add(placeId)
                            }
                        }
                        routeAdapter.routePlaceIds[routeName] = placeIds
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                routeAdapter.updateData(routeNames)
            },
            { error ->
                // 오류 처리
            })

        requestQueue.add(jsonArrayRequest)
    }

    private fun updateAdapterData(routeNames: List<String>) {
        routeAdapter.updateData(routeNames)
    }

    private fun setupSaveRouteButton() {
        val saveButton = findViewById<Button>(R.id.route_save_btn)
        saveButton.setOnClickListener {
            val selectedRouteName = routeAdapter.getSelectedRouteName()
            if (selectedRouteName.isNotEmpty()) {
                val token = getSavedToken() // JWT 토큰을 가져오는 메서드
                fetchUserId(token) { userId ->
                    val placeIds = routeAdapter.getPlaceIdsForRoute(selectedRouteName)
                    sendRouteData(userId, selectedRouteName, placeIds)
                }
            } else {
                Toast.makeText(this, "먼저 루트를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSavedToken(): String {
        // 'MyApp' 파일에서 SharedPreferences 객체를 가져옵니다.
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        // 'token' 키에 해당하는 값을 검색합니다. 토큰이 없는 경우 기본값으로 빈 문자열("")을 반환합니다.
        return sharedPref.getString("token", "") ?: ""
    }

    private fun sendRouteData(userId: Long, routeName: String, placeIds: List<Int>) {
        val url = "https://belleravi.co.kr/route-create"
        val postData = JSONObject().apply {
            put("userId", userId)
            put("routename", routeName)
            for (i in placeIds.indices) {
                put("p${i+1}", placeIds[i])
            }
        }.toString() // JSON 객체를 문자열로 변환

        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,
            Response.Listener { response ->
                // 성공 처리
                Toast.makeText(this, "루트가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // 에러 처리
                Log.d("log", postData)
                Toast.makeText(this, "오류: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBody(): ByteArray {
                return postData.toByteArray(Charset.forName("UTF-8"))
            }

            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(stringRequest)
    }


    private fun fetchUserId(token: String, onSuccess: (Long) -> Unit) {
        val url = "https://belleravi.co.kr/user-info"

        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                val userId = parseUserId(response)
                onSuccess(userId)
                Log.d("log", userId.toString())
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "오류: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun parseUserId(response: String): Long {
        // 서버 응답으로부터 사용자 ID를 파싱하는 로직
        // 예: JSONObject로 변환하고 "id" 필드를 읽음
        val jsonObject = JSONObject(response)
        return jsonObject.getLong("id")
    }
    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRoutes)

        routeAdapter = RouteAdapter(
            context = this,
            routeNames = mutableListOf(),
            onItemAdded = { index ->
                // 아이템 추가 후에 수행할 작업, 예를 들어 스크롤 조정
                recyclerView.scrollToPosition(index)
            },
            onItemsUpdated = { hasItems ->
                val savebutton = findViewById<Button>(R.id.route_save_btn)
                val guidebutton = findViewById<Button>(R.id.guide_btn)
                if (hasItems) {
                    savebutton.setBackgroundResource(R.drawable.route_button_style)
                    savebutton.setTextColor(Color.WHITE)
                    savebutton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.edit_location, 0, 0, 0
                    )
                    savebutton.isEnabled = true // 버튼 활성화

                    guidebutton.setBackgroundResource(R.drawable.route_button_style)
                    guidebutton.setTextColor(Color.WHITE)
                    guidebutton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.edit_location, 0, 0, 0
                    )
                    guidebutton.isEnabled = true
                } else {
                    savebutton.setTextColor(Color.parseColor("#308FFA"))
                    savebutton.setBackgroundResource(R.drawable.route_nonebutton_style)
                    savebutton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.noneedit_location, 0, 0, 0
                    )
                    savebutton.isEnabled = false // 버튼 비활성화

                    guidebutton.setBackgroundResource(R.drawable.route_nonebutton_style)
                    guidebutton.setTextColor(Color.parseColor("#308FFA"))
                    guidebutton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.noneedit_location, 0, 0,0
                    )

                    guidebutton.isEnabled = false
                }
            }
        )

        recyclerView.adapter = routeAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupSearchQuery() {
        intent.getStringExtra("SEARCH_QUERY")?.let { searchQuery ->
            sendSearchQuery(searchQuery)
        } ?: getCurrentLocation()
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
            placeCardFragment.updatePlacesList(places)
            placeCardFragment.updatePlacesQuery(query)

            // 검색 결과의 첫 번째 장소에 마커 추가 및 카메라 이동
            if (places.isNotEmpty()) {
                moveToLocationAndAddMarker(places[0])
            }
        }, { error ->
            // 에러 발생 시 처리
            error.printStackTrace()
        })

        requestQueue.add(stringRequest)
    }


    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()
    }

    private fun setupBottomSheet() {
        val bottomSheet = findViewById<FrameLayout>(R.id.placeCardContainer)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        val peakHeightDp = 280
        val peakHeightPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, peakHeightDp.toFloat(), resources.displayMetrics
        ).toInt()
        bottomSheetBehavior.peekHeight = peakHeightPixels

        // 바텀시트가 접혔을 때 높이를 30dp로 설정
        val collapsedHeightDp = 30
        val collapsedHeightPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, collapsedHeightDp.toFloat(), resources.displayMetrics
        ).toInt()

        // 바텀시트의 상태 변경 콜백을 설정
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // 접혔을 때 높이를 설정합니다.
                    bottomSheetBehavior.peekHeight = collapsedHeightPixels
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            // 여기서 padding을 설정합니다.
            val topPadding = 200
            val rightPadding = 0
            val bottomPadding = 0
            val leftPadding = 0 // 왼쪽에 padding을 주어 버튼 위치를 변경합니다.
            mMap.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
            setupSearchQuery()
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    searchForCurrent(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun searchForCurrent(latitude: Double, longitude: Double) {
        val url = "https://belleravi.co.kr/current?latitude=${latitude}&longitude=${longitude}"

        // Volley 요청 큐
        val requestQueue = Volley.newRequestQueue(this)

        // StringRequest 생성 및 요청
        val stringRequest =
            StringRequest(Request.Method.GET, url, { response ->
                Log.d("RawResponse", response) // 로그에 원본 응답 출력
                val correctEncodingResponse =
                    String(response.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
                try {
                    val places = parseJsonToPlaces(correctEncodingResponse)
                    placeCardFragment.updatePlacesList(places)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    // 문자열이 JSON 형식이 아닐 경우 처리.
                }
            }, { error ->
                // 에러 처리
                Log.e("Error", error.toString())
            })

        requestQueue.add(stringRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun parseJsonToPlaces(jsonString: String): List<Place> {
        val gson = Gson()
        val type = object : TypeToken<List<Place>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun moveToLocationAndAddMarker(place: Place) {
        currentMarker?.remove()

        val location = LatLng(place.mapy, place.mapx)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))

        currentMarker = mMap.addMarker(MarkerOptions().position(location).title(place.title))
        mMap.setOnInfoWindowClickListener { marker ->
            val selectedRouteName = routeAdapter.getSelectedRouteName()
            if (selectedRouteName.isNotEmpty()) {
                routeAdapter.addPlaceIdToRoute(selectedRouteName, place.id)
            } else {
                Toast.makeText(this, "먼저 루트를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
