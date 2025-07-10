package com.example.checklistkotlin

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    //task key é uma constante para melhorar a legibilidade
    // e impedir modificações acidentais no valor da chave.
    companion object {
        private const val TASKS_KEY = "task_list"
    }

    // Salva lista de tarefas como JSON em SharedPreferences
    fun saveTasks(taskList: List<Task>) {
        // Converte a lista de objetos Task em uma string JSON
        val json = gson.toJson(taskList)
        // Tenta recuperar a string JSON salva. Se não houver nada sob "task_list", retorna null.
        prefs.edit().putString(TASKS_KEY, json).apply()
    }

    // Carrega JSON de volta para lista de Task
    fun loadTasks(): List<Task> {
        // Lê a string JSON do SharedPreferences. Se for null, retorna lista vazia.
        val json = prefs.getString(TASKS_KEY, null) ?: return emptyList()
        // Cria um objeto Type que representa List<Task>, usando um TypeToken anônimo.
        val type = object : TypeToken<List<Task>>() {}.type
        // Converte a string JSON de volta em List<Task> usando Gson
        return gson.fromJson(json, type)
    }
}
