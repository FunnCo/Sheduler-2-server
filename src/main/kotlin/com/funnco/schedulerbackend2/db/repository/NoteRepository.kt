package com.funnco.schedulerbackend2.db.repository

import com.funnco.schedulerbackend2.db.entity.EventsCurrentEntity
import com.funnco.schedulerbackend2.db.entity.NoteEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface NoteRepository: CrudRepository<NoteEntity, UUID> {
    fun deleteAllByEventId(eventId: UUID)
}