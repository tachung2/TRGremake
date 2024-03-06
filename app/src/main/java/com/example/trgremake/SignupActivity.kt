package com.example.trgremake

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.pwInput)
        val nextButton : ImageView = findViewById(R.id.btnNext)

        nextButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val intent = Intent(this, SignupDetailActivity::class.java).apply {
                putExtra("email", email)
                putExtra("password", password)
            }
            startActivity(intent)
        }

        val back_btn : ImageView = findViewById(R.id.btnBack)
        back_btn.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}