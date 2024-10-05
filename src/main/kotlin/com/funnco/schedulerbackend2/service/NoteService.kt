package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.NoteEntity
import com.funnco.schedulerbackend2.db.repository.CurrentEventsRepository
import com.funnco.schedulerbackend2.db.repository.NoteRepository
import com.funnco.schedulerbackend2.dto.NoteDTO
import com.funnco.schedulerbackend2.utils.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*


@Service
class NoteService {

    private val className = this::class.java.simpleName
    private val log = LoggerFactory.getLogger(className)

    @Autowired
    private lateinit var currentEventsRepository: CurrentEventsRepository

    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Transactional
    fun addNote(dto: NoteDTO, eventId: String) {
        try {
            log.info("[$className] Start creating note for $eventId")
            val event = currentEventsRepository.findByIdOrNull(UUID.fromString(eventId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "event with such id is not found")
            val note = NoteEntity()
            note.id = UUID.randomUUID()
            note.flag = dto.flag
            note.content = dto.content
            note.event = event
            noteRepository.save(note)
            log.info("[$className] End creating note ${note.id} for $eventId")
        } catch (any: Exception) {
            log.error("[$className] Error while creating note for $eventId", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    @Transactional
    fun updateNote(note: NoteDTO) {
        try {
            log.info("[$className] Start updating note ${note.id}")
            val noteEntity = noteRepository.findByIdOrNull(UUID.fromString(note.id))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "note with such id is not found")
            noteEntity.flag = note.flag
            noteEntity.content = note.content
            noteRepository.save(noteEntity)
            log.info("[$className] End updating note ${note.id} ")
        } catch (any: Exception) {
            log.error("[$className] Error while updating note: ${note.id}", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    @Transactional
    fun deleteNote(noteId: String) {
        try {
            log.info("[$className] Start deleting note $noteId")
            noteRepository.deleteById(UUID.fromString(noteId))
            log.info("[$className] End delete note $noteId ")
        } catch (any: Exception) {
            log.error("[$className] Error while deleting note: $noteId", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    @Transactional
    fun deleteAllEventNotes(eventId: String) {
        try {
            log.info("[$className] Start deleting notes by event $eventId")
            noteRepository.deleteAllByEventId(UUID.fromString(eventId))
            log.info("[$className] End delete notes by event $eventId ")
        } catch (any: Exception) {
            log.error("[$className] Error while deleting notes by events: $eventId", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

}