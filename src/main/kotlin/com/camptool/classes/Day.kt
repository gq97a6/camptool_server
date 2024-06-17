package com.camptool.classes

import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.random.Random

data class Day(
    var uuid: String = "",
    val date: LocalDate = LocalDate.MAX,
    val events: List<DayEvent> = listOf()
)

data class DayEvent(
    val time: LocalTime = LocalTime.of(0, 0),
    val des: String = "",
    val group: String = ""
)

val days = generateListOfDays()

fun generateListOfDays(): List<Day> {
    val startDate = LocalDate.of(2024, 7, 10)
    val activities = listOf("Śniadanie", "Zbiórka", "Gry i zabawy", "Lekcja pływania", "Obiad", "Wycieczka", "Ognisko")

    return (0 until 10).map { dayOffset ->
        val date = startDate.plusDays(dayOffset.toLong())
        val events = generateDayEvents(activities)

        Day(
            UUID.randomUUID().toString().replace("-", "").uppercase(Locale.getDefault()),
            date,
            events
        )
    }
}

fun generateDayEvents(activities: List<String>): List<DayEvent> {

    val events = mutableListOf<DayEvent>()
    var time = LocalTime.of(8, 0)
    var lastGrouped = false
    repeat(10) {
        val isGrouped = Random.nextInt(10) <= 1
        val activity = activities.random()

        val group = when {
            lastGrouped -> "B"
            isGrouped -> "A"
            else -> ""
        }

        lastGrouped = isGrouped

        events.add(DayEvent(time = time, des = activity, group = group))
        if (!isGrouped) time = time.plusMinutes(listOf(30L, 60L).random())
    }

    return events
}