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

    fun saveTasks(taskList: List<Task>) {
        val json = gson.toJson(taskList)
        prefs.edit().putString(TASKS_KEY, json).apply()
    }

    fun loadTasks(): List<Task> {
        val json = prefs.getString(TASKS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Task>>() {}.type
        return gson.fromJson(json, type)
    }
}
