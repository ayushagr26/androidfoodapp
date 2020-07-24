package com.example.khazana.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.example.khazana.R

class SplashScreen : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed(
            {
                val i = Intent(this@SplashScreen, LogIn::class.java)
                startActivity(i)
                finish()
            }, 1000
        )
    }
}