package com.example.checklistkotlin

import java.time.LocalDate

data class Task(
    var text: String,
    var done: Boolean = false,
    val date: LocalDate = LocalDate.now()
) {
    // Construtor secundário que só recebe o texto
    constructor(text: String) : this(text, false, LocalDate.now())
}
