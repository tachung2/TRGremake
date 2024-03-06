package com.example.trgremake

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPre = getSharedPreferences("MyApp", MODE_PRIVATE)
        val token = sharedPre.getString("token", null)

        if (token == null) {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}