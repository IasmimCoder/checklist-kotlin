package com.example.checklistkotlin

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    protected lateinit var bottomNavigation: BottomNavigationView

    override fun setContentView(layoutResID: Int) {
        val fullView = layoutInflater.inflate(R.layout.activity_base, null) as LinearLayout
        val container = fullView.findViewById<FrameLayout>(R.id.container)
        layoutInflater.inflate(layoutResID, container, true)
        super.setContentView(fullView)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_pomodoro -> {
                    if (this !is FocusTimerActivity) {
                        startActivity(Intent(this, FocusTimerActivity::class.java))
                        overridePendingTransition(0,0)
                        finish()
                    }
                    true
                }
                R.id.nav_tasks -> {
                    if (this !is MainActivity) {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(0,0)
                        finish()
                    }
                    true
                }
                R.id.nav_stats -> {
                    if (this !is StatisticsActivity) {
                        startActivity(Intent(this, StatisticsActivity::class.java))
                        overridePendingTransition(0,0)
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
