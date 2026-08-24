package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.EventsCurrentEntity
import com.funnco.schedulerbackend2.db.entity.NoteEntity
import com.funnco.schedulerbackend2.db.repository.CurrentEventsRepository
import com.funnco.schedulerbackend2.db.repository.NoteRepository
import com.funnco.schedulerbackend2.dto.NoteDTO
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.server.ResponseStatusException
import java.util.*

@ExtendWith(MockKExtension::class)
class NoteServiceTest {

    @MockK
    lateinit var currentEventsRepository: CurrentEventsRepository

    @MockK
    lateinit var noteRepository: NoteRepository

    @InjectMockKs
    lateinit var noteService: NoteService

    @Test
    fun `addNote should create note for existing event`() {
        val eventId = UUID.randomUUID()
        val event = EventsCurrentEntity().apply { id = eventId }
        every { currentEventsRepository.findByIdOrNull(eventId) } returns event
        val captured = slot<NoteEntity>()
        every { noteRepository.save(capture(captured)) } answers { captured.captured }

        noteService.addNote(NoteDTO(content = "My note", flag = true), eventId.toString())

        assertThat(captured.captured.content).isEqualTo("My note")
        assertThat(captured.captured.flag).isTrue()
        assertThat(captured.captured.event).isEqualTo(event)
        assertThat(captured.captured.id).isNotNull()
    }

    @Test
    fun `addNote should throw NOT_FOUND when event does not exist`() {
        val eventId = UUID.randomUUID()
        every { currentEventsRepository.findByIdOrNull(eventId) } returns null

        val exception = assertThrows<ResponseStatusException> {
            noteService.addNote(NoteDTO(content = "My note"), eventId.toString())
        }

        assertThat(exception.statusCode.value()).isEqualTo(404)
    }

    @Test
    fun `updateNote should update content and flag`() {
        val noteId = UUID.randomUUID()
        val existingNote = NoteEntity().apply {
            id = noteId
            content = "Old content"
            flag = false
        }
        every { noteRepository.findByIdOrNull(noteId) } returns existingNote
        val captured = slot<NoteEntity>()
        every { noteRepository.save(capture(captured)) } answers { captured.captured }

        noteService.updateNote(NoteDTO(id = noteId.toString(), content = "New content", flag = true))

        assertThat(captured.captured.content).isEqualTo("New content")
        assertThat(captured.captured.flag).isTrue()
    }

    @Test
    fun `updateNote should throw NOT_FOUND when note does not exist`() {
        val noteId = UUID.randomUUID()
        every { noteRepository.findByIdOrNull(noteId) } returns null

        val exception = assertThrows<ResponseStatusException> {
            noteService.updateNote(NoteDTO(id = noteId.toString(), content = "New content"))
        }

        assertThat(exception.statusCode.value()).isEqualTo(404)
    }

    @Test
    fun `deleteNote should delete note by id`() {
        val noteId = UUID.randomUUID()
        every { noteRepository.deleteById(noteId) } just runs

        noteService.deleteNote(noteId.toString())

        verify { noteRepository.deleteById(noteId) }
    }

    @Test
    fun `deleteAllEventNotes should delete notes by event id`() {
        val eventId = UUID.randomUUID()
        every { noteRepository.deleteAllByEventId(eventId) } just runs

        noteService.deleteAllEventNotes(eventId.toString())

        verify { noteRepository.deleteAllByEventId(eventId) }
    }
}
