package com.example.checklistkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import android.widget.TextView
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var tvHoje: TextView
    private lateinit var tvSemana: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        pieChart = findViewById(R.id.pieChart)
        tvHoje = findViewById(R.id.tvHoje)
        tvSemana = findViewById(R.id.tvSemana)

        val storage = TaskStorage(this)
        val tasks = storage.loadTasks()

        // Contagem de tarefas
        val concluidas = tasks.count { it.done }
        val pendentes = tasks.count { !it.done }

        // Gráfico de Pizza
        val entries = listOf(
            PieEntry(concluidas.toFloat(), "Concluídas"),
            PieEntry(pendentes.toFloat(), "Pendentes")
        )
        val dataSet = PieDataSet(entries, "Status")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.animateY(1000)
        pieChart.invalidate()

        // Tarefas do dia
        val hoje = LocalDate.now()
        val tarefasHoje = tasks.count { it.date == hoje }
        tvHoje.text = "Tarefas de hoje: $tarefasHoje"

        // Tarefas da semana
        val semanaAtual = hoje.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
        val tarefasSemana = tasks.count {
            it.date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) == semanaAtual
        }
        tvSemana.text = "Tarefas da semana: $tarefasSemana"
    }
}