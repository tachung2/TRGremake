package com.example.trgremake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val back_btn : ImageButton = findViewById(R.id.back_btn)
        back_btn.setOnClickListener {
            onBackPressed()
        }

        val editId: EditText = findViewById(R.id.edit_id)
        val editPw: EditText = findViewById(R.id.edit_pw)
        val loginButton: Button = findViewById(R.id.btn_login)
        val signupButton: Button = findViewById(R.id.btn_signup)

        loginButton.setOnClickListener {
            val userId = editId.text.toString().trim()
            val password = editPw.text.toString().trim()

            if (userId.isNotEmpty() && password.isNotEmpty()) {
                login(userId, password)
            } else {
                Toast.makeText(this, "ID와 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show()
            }
        }
        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(userId: String, password: String) {
        val url = "https://belleravi.co.kr/login"
        val requestQueue = Volley.newRequestQueue(this)

        val params = JSONObject()
        params.put("email", userId)
        params.put("password", password)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                val token = response.getString("token")
                val sharedPre = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                with (sharedPre.edit()) {
                    putString("token", token)
                    apply()
                    next()
                }
                // 성공 처리: 응답 구조에 따라 달라질 수 있음
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show()
            },
            { error ->
                // 에러 처리
                Log.e("LoginError", "Error: ${error.toString()}") // 로그 출력
                if (error.networkResponse != null) {
                    Log.e("LoginError", "Error Response code: ${error.networkResponse.statusCode}") // 상태 코드 로그
                    Toast.makeText(this, "서버 오류: ${error.networkResponse.statusCode}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "네트워크 오류", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // 요청 큐에 추가
        requestQueue.add(jsonObjectRequest)
    }

    private fun next() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}