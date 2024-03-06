package com.example.trgremake

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SignupDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_detail)

        val backbtn : ImageView = findViewById(R.id.back_btn)

        backbtn.setOnClickListener {
            onBackPressed()
        }

        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")
        val name : EditText = findViewById(R.id.nameInput)
        val checkButton : ImageView = findViewById(R.id.checkbtn)

        checkButton.setOnClickListener {
            val nameText = name.text.toString().trim()
            if(nameText.isNotEmpty()){
                if (email != null && password != null) {
                    sendSignUpRequest(email, password, nameText)
                }
            } else {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun sendSignUpRequest(email: String?, password: String?, name: String?) {
        val url = "https://belleravi.co.kr/signup"
        val requestQueue = Volley.newRequestQueue(this)

        val params = JSONObject()
        params.put("email", email)
        params.put("password", password)
        params.put("name", name)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                Toast.makeText(this, "회원가입에 성공했습니다.", Toast.LENGTH_LONG).show()
                next()
            },
            { error ->
                Toast.makeText(this, "중복되는 아이디가 있습니다.", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)

    }
    private fun next() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}