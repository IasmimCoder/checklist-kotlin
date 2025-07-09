package com.example.checklistkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView

class StatisticsActivity : BaseActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var textViewTotal: TextView
    private lateinit var taskStorage: TaskStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        pieChart = findViewById(R.id.pieChart)
        textViewTotal = findViewById(R.id.textViewTotal)
        taskStorage = TaskStorage(this)

        val tasks = taskStorage.loadTasks()
        val doneCount = tasks.count { it.done }
        val pendingCount = tasks.count { !it.done }

        // Atualiza total
        textViewTotal.text = "Total de tarefas: ${tasks.size}"

        // Configura gráfico
        val entries = listOf(
            PieEntry(doneCount.toFloat(), "Concluídas"),
            PieEntry(pendingCount.toFloat(), "Pendentes")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = android.graphics.Color.WHITE

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Tarefas"
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.animateY(1000)

        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        // Navegação inferior
        bottomNavigation.selectedItemId = R.id.nav_stats

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_tasks -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_pomodoro -> {
                    val intent = Intent(this, FocusTimerActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_stats -> true
                else -> false
            }
        }
    }
}
