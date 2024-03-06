package com.example.trgremake

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView

class IntroActivity : AppCompatActivity() {
    private var videoView: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro)

        videoView = findViewById(R.id.videoView)
        setupVideoView()

        val loginButton: Button = findViewById(R.id.login_btn)
        loginButton.setOnClickListener {
            navigateToLoginActivity()
        }
        val registerButton: Button = findViewById(R.id.register_btn)
        registerButton.setOnClickListener {
            navigateToSignupActivity()
        }
    }

    private fun setupVideoView() {
        val uri = Uri.parse("android.resource://$packageName/${R.raw.travel}")
        videoView?.apply {
            setVideoURI(uri)
            setOnPreparedListener {mediaPlayer ->
                val aspectRatio = mediaPlayer.videoWidth.toFloat() / mediaPlayer.videoHeight
                val screenHeight = resources.displayMetrics.heightPixels
                val newWidth = (screenHeight * aspectRatio).toInt()
                layoutParams = layoutParams.apply {
                    width = newWidth
                    height = screenHeight // 화면 높이에 맞춤
                }
                x = ((newWidth - resources.displayMetrics.widthPixels) / 2f) * -1 // 화면 중앙 정렬

                mediaPlayer.setVolume(0f, 0f)
            }
            setOnCompletionListener {
                start()
            }
            start()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        videoView?.stopPlayback()
    }

    private fun navigateToSignupActivity() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        videoView?.stopPlayback()
    }

    override fun onPause() {
        super.onPause()
        videoView?.stopPlayback()
    }

    override fun onResume() {
        super.onResume()
        if (videoView?.isPlaying == false) {
            videoView?.start()
        }
    }

    override fun onBackPressed() {
        videoView?.stopPlayback()
        super.onBackPressed()
    }
}