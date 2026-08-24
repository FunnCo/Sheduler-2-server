package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.EventsCurrentEntity
import com.funnco.schedulerbackend2.db.entity.EventsTemplateEntity
import com.funnco.schedulerbackend2.db.entity.NoteEntity
import com.funnco.schedulerbackend2.db.entity.UserEntity
import com.funnco.schedulerbackend2.db.model.WeekDay
import com.funnco.schedulerbackend2.db.repository.CurrentEventsRepository
import com.funnco.schedulerbackend2.db.repository.TemplateEventsRepository
import com.funnco.schedulerbackend2.dto.EventDTO
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
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.OffsetTime
import java.util.*

@ExtendWith(MockKExtension::class)
class CurrentEventsServiceTest {

    @MockK
    lateinit var noteService: NoteService

    @MockK
    lateinit var templateEventsRepository: TemplateEventsRepository

    @MockK
    lateinit var currentEventsRepository: CurrentEventsRepository

    @MockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var currentEventsService: CurrentEventsService

    private val userId = UUID.randomUUID().toString()
    private val user = UserEntity().apply {
        id = UUID.fromString(userId)
        name = "Alice"
    }

    @Test
    fun `addSingleEvent should create and save event`() {
        every { userService.getUserById(userId) } returns user
        val captured = slot<EventsCurrentEntity>()
        every { currentEventsRepository.save(capture(captured)) } answers { captured.captured }

        val dto = EventDTO(
            description = "Dentist",
            startTime = OffsetTime.parse("09:00:00+00:00"),
            endTime = OffsetTime.parse("10:00:00+00:00"),
            date = LocalDate.of(2024, 9, 10)
        )

        currentEventsService.addSingleEvent(userId, dto)

        assertThat(captured.captured.description).isEqualTo("Dentist")
        assertThat(captured.captured.user).isEqualTo(user)
        assertThat(captured.captured.id).isNotNull()
    }

    @Test
    fun `updateSingleEvent should update existing event`() {
        val eventId = UUID.randomUUID()
        val event = EventsCurrentEntity().apply { id = eventId }
        every { currentEventsRepository.findById(eventId) } returns Optional.of(event)
        val captured = slot<EventsCurrentEntity>()
        every { currentEventsRepository.save(capture(captured)) } answers { captured.captured }

        val dto = EventDTO(
            id = eventId.toString(),
            description = "Updated",
            startTime = OffsetTime.parse("14:00:00+00:00"),
            endTime = OffsetTime.parse("15:00:00+00:00"),
            date = LocalDate.of(2024, 9, 11)
        )

        currentEventsService.updateSingleEvent(dto)

        assertThat(captured.captured.description).isEqualTo("Updated")
        assertThat(captured.captured.date).isEqualTo(LocalDate.of(2024, 9, 11))
    }

    @Test
    fun `updateSingleEvent should throw NOT_FOUND when event does not exist`() {
        val eventId = UUID.randomUUID()
        every { currentEventsRepository.findById(eventId) } returns Optional.empty()

        val exception = assertThrows<ResponseStatusException> {
            currentEventsService.updateSingleEvent(EventDTO(id = eventId.toString()))
        }

        assertThat(exception.statusCode.value()).isEqualTo(404)
    }

    @Test
    fun `deleteSingleEvent should delete event and its notes`() {
        val eventId = UUID.randomUUID()
        every { noteService.deleteAllEventNotes(eventId.toString()) } just runs
        every { currentEventsRepository.deleteById(eventId) } just runs

        currentEventsService.deleteSingleEvent(eventId.toString())

        verify { noteService.deleteAllEventNotes(eventId.toString()) }
        verify { currentEventsRepository.deleteById(eventId) }
    }

    @Test
    fun `getEventsForUser should return list of event DTOs`() {
        val event = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.of(2024, 9, 10)
            user = this@CurrentEventsServiceTest.user
        }
        every { userService.getUserById(userId) } returns user
        every { currentEventsRepository.getEventsCurrentsByUser(user) } returns listOf(event)

        val result = currentEventsService.getEventsForUser(userId)

        assertThat(result).hasSize(1)
        assertThat(result[0].description).isEqualTo("Meeting")
    }

    @Test
    fun `deleteAllFutureEvents should delete future events and their notes`() {
        val futureEvent = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            date = LocalDate.now().plusDays(1)
            noteEntities = mutableSetOf(
                NoteEntity().apply { id = UUID.randomUUID() }
            )
        }
        val pastEvent = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            date = LocalDate.now().minusDays(1)
        }
        every { currentEventsRepository.findAll() } returns listOf(futureEvent, pastEvent)
        every { noteService.deleteNote(any()) } just runs
        every { currentEventsRepository.delete(futureEvent) } just runs

        currentEventsService.deleteAllFutureEvents()

        verify { noteService.deleteNote(futureEvent.noteEntities.first().id.toString()) }
        verify { currentEventsRepository.delete(futureEvent) }
    }

    @Test
    fun `forceCreateFutureEventsByTemplate should create events from all templates`() {
        val template = EventsTemplateEntity().apply {
            id = UUID.randomUUID()
            description = "Weekly sync"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            weekDay = WeekDay.MONDAY
            user = this@CurrentEventsServiceTest.user
        }
        every { templateEventsRepository.findAll() } returns listOf(template)
        val captured = slot<List<EventsCurrentEntity>>()
        every { currentEventsRepository.saveAll(capture(captured)) } returnsArgument 0

        currentEventsService.forceCreateFutureEventsByTemplate(2)

        assertThat(captured.captured).isNotEmpty()
        captured.captured.forEach { event ->
            assertThat(event.template).isEqualTo(template)
            assertThat(event.user).isEqualTo(user)
        }
    }
}
