package com.example.checklistkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.widget.CheckBox

class MainActivity : AppCompatActivity() {

    private lateinit var cbWakeUp: CheckBox
    private lateinit var cbBreakfast: CheckBox
    private lateinit var cbStudy: CheckBox
    private lateinit var cbWorkout: CheckBox
    private lateinit var cbWater: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referências
        cbWakeUp = findViewById(R.id.cbWakeUp)
        cbBreakfast = findViewById(R.id.cbBreakfast)
        cbStudy = findViewById(R.id.cbStudy)
        cbWorkout = findViewById(R.id.cbWorkout)
        cbWater = findViewById(R.id.cbWater)

        // Carrega estado salvo
        loadCheckStates()

        // Salva estado ao clicar
        setupListeners()
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE)

        listOf(cbWakeUp, cbBreakfast, cbStudy, cbWorkout, cbWater).forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(checkBox.id.toString(), isChecked).apply()
            }
        }
    }

    private fun loadCheckStates() {
        val prefs = getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE)

        listOf(cbWakeUp, cbBreakfast, cbStudy, cbWorkout, cbWater).forEach { checkBox ->
            val saved = prefs.getBoolean(checkBox.id.toString(), false)
            checkBox.isChecked = saved
        }
    }
}
