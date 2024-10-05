package com.funnco.schedulerbackend2.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.funnco.schedulerbackend2.db.model.WeekDay
import java.time.OffsetTime


data class TemplateEventDTO(
    var id: String? = null,
    var description: String? = null,
    var startTime: OffsetTime? = null,
    var endTime: OffsetTime? = null,
    var day: String? = null
) {

    @JsonIgnore
    fun isValid(): Boolean {
        return !description.isNullOrBlank()
                && WeekDay.entries.stream().anyMatch { it.name == day }
                && startTime != null
                && endTime != null
                && startTime!!.isBefore(endTime!!)
    }
}