package com.example.trgremake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appproject.adapter.RecoRVAdapter
import com.example.appproject.datas.RecoData
import com.example.trgremake.R
import com.example.trgremake.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        drawerLayout = binding.drawerLayout
        navView = binding.navView

        binding.menu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        displayRV()

        binding.imgProfile.setOnClickListener {
            logout()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            Log.d("NavigationDrawer", "Clicked item: ${menuItem.itemId}")

            when (menuItem.itemId) {
                R.id.navi_route -> {
                    route()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.Search.setOnFocusChangeListener {v, hasFocus ->
            if(hasFocus) {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
                binding.Search.clearFocus()
            }
        }
    }

    private fun route() {
        Log.d("MainActivity", "route")
        val intent = Intent(this, RouteActivity::class.java)
        startActivity(intent)
    }

    private fun displayRV(){
        val reco = ArrayList<RecoData>()
        reco.addAll(RecoData.getList())
        binding.rvRecommendList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rvRecommendList.adapter = RecoRVAdapter(reco)
    }

    private fun logout() {
        val sharedPre = getSharedPreferences("MyApp", MODE_PRIVATE)
        with(sharedPre.edit()) {
            remove("token")
            apply()
        }

        val intent = Intent(this, IntroActivity::class.java)
        startActivity(intent)
        finish()
    }


}