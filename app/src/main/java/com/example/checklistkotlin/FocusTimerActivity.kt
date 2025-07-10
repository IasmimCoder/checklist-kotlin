package com.example.checklistkotlin

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView

class FocusTimerActivity : BaseActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnReset: Button

    private var timer: CountDownTimer? = null
    private var isRunning = false

    private val startTimeInMillis: Long = 25 * 60 * 1000 // 25 minutos
    private var timeLeftInMillis = startTimeInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_timer)

        bottomNavigation.selectedItemId = R.id.nav_pomodoro

        tvTimer = findViewById(R.id.tvTimer)
        btnStartPause = findViewById(R.id.btnStartPause)
        btnReset = findViewById(R.id.btnReset)

        btnStartPause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        updateCountDownText()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isRunning = false
                btnStartPause.text = "Iniciar"
                timeLeftInMillis = startTimeInMillis
                updateCountDownText()
                // Aqui você pode tocar um som ou notificação
            }
        }.start()

        isRunning = true
        btnStartPause.text = "Pausar"
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        btnStartPause.text = "Iniciar"
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = startTimeInMillis
        updateCountDownText()
        btnStartPause.text = "Iniciar"
        isRunning = false
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        tvTimer.text = timeFormatted
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
