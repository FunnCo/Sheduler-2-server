package com.funnco.schedulerbackend2.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.funnco.schedulerbackend2.db.entity.EventsCurrentEntity
import com.funnco.schedulerbackend2.db.entity.NoteEntity
import com.funnco.schedulerbackend2.db.model.WeekDay
import java.time.LocalDate
import java.time.OffsetTime
import java.util.stream.Collectors

data class EventDTO(
    var id: String? = null,
    var description: String? = null,
    var startTime: OffsetTime? = null,
    var endTime: OffsetTime? = null,
    var notes: List<NoteDTO>? = null,
    var date: LocalDate? = null
) {
    constructor(eventEntity: EventsCurrentEntity) : this(
        id = eventEntity.id.toString(),
        description = eventEntity.description,
        startTime = eventEntity.startTime,
        endTime = eventEntity.endTime,
        date = eventEntity.date,
        notes = eventEntity.noteEntities
            .stream()
            .map { note -> NoteDTO(note) }
            .toList()
    )

    @JsonIgnore
    fun isValid(): Boolean{
        return startTime != null && endTime != null
                && startTime!!.isBefore(endTime)
    }
}