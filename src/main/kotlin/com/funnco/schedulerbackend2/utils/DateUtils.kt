package com.funnco.schedulerbackend2.utils

import java.time.LocalDate

object DateUtils {

    fun getCurrentWeekStartDate(): LocalDate {
        return LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong())
    }
}