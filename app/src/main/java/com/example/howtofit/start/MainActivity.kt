package com.example.howtofit.start

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.howtofit.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val maglia: ImageView= findViewById(R.id.imageView)
        val puntoInt: ImageView = findViewById(R.id.imageView2)
        val scritta: ImageView = findViewById(R.id.imageView3)
        val anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animazione)
        val anim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animazione2)

        anim2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // L'animazione è terminata, passa alla LoginActivity
                val intentLoginActivity = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intentLoginActivity)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

            maglia.startAnimation(anim)
            puntoInt.startAnimation(anim)
            scritta.startAnimation(anim2)

    }
}

