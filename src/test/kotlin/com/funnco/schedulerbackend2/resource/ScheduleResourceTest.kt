package com.funnco.schedulerbackend2.resource

import com.funnco.schedulerbackend2.db.entity.EventsCurrentEntity
import com.funnco.schedulerbackend2.db.entity.EventsTemplateEntity
import com.funnco.schedulerbackend2.db.entity.NoteEntity
import com.funnco.schedulerbackend2.db.entity.UserEntity
import com.funnco.schedulerbackend2.db.model.WeekDay
import com.funnco.schedulerbackend2.db.repository.CurrentEventsRepository
import com.funnco.schedulerbackend2.db.repository.NoteRepository
import com.funnco.schedulerbackend2.db.repository.TemplateEventsRepository
import com.funnco.schedulerbackend2.db.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.OffsetTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleResourceTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var currentEventsRepository: CurrentEventsRepository

    @Autowired
    lateinit var templateEventsRepository: TemplateEventsRepository

    @Autowired
    lateinit var noteRepository: NoteRepository

    @Autowired
    lateinit var userRepository: UserRepository

    private lateinit var userId: String

    @BeforeEach
    fun setUp() {
        noteRepository.deleteAll()
        currentEventsRepository.deleteAll()
        templateEventsRepository.deleteAll()
        userRepository.deleteAll()

        val user = UserEntity().apply {
            id = UUID.randomUUID()
            name = "Test User"
        }
        userRepository.save(user)
        userId = user.id.toString()
    }

    @Test
    fun `upsertTemplate should create template and current events in database`() {
        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Weekly meeting",
                        "startTime": "10:00:00+00:00",
                        "endTime": "11:00:00+00:00",
                        "day": "MONDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        val templates = templateEventsRepository.findAll().toList()
        assertThat(templates).hasSize(1)
        val template = templates.first()
        assertThat(template.description).isEqualTo("Weekly meeting")
        assertThat(template.weekDay).isEqualTo(WeekDay.MONDAY)
        assertThat(template.startTime?.toLocalTime()).isEqualTo(OffsetTime.parse("10:00:00+00:00").toLocalTime())
        assertThat(template.endTime?.toLocalTime()).isEqualTo(OffsetTime.parse("11:00:00+00:00").toLocalTime())
        assertThat(template.user?.id.toString()).isEqualTo(userId)

        val events = currentEventsRepository.findAll().toList()
        assertThat(events).hasSize(5)
        events.forEach { event ->
            assertThat(event.description).isEqualTo("Weekly meeting")
            assertThat(event.startTime?.toLocalTime()).isEqualTo(OffsetTime.parse("10:00:00+00:00").toLocalTime())
            assertThat(event.endTime?.toLocalTime()).isEqualTo(OffsetTime.parse("11:00:00+00:00").toLocalTime())
            assertThat(event.user?.id.toString()).isEqualTo(userId)
            assertThat(event.template?.id).isEqualTo(template.id)
        }
    }

    @Test
    fun `upsertTemplate should return BAD_REQUEST for invalid dto and not touch database`() {
        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "",
                        "startTime": "10:00:00+00:00",
                        "endTime": "11:00:00+00:00",
                        "day": "MONDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)

        assertThat(templateEventsRepository.findAll()).isEmpty()
        assertThat(currentEventsRepository.findAll()).isEmpty()
    }

    @Test
    fun `upsertTemplate should update existing template in database`() {
        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Weekly meeting",
                        "startTime": "10:00:00+00:00",
                        "endTime": "11:00:00+00:00",
                        "day": "MONDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        val templateId = templateEventsRepository.findAll().first().id

        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "$templateId",
                        "description": "Updated meeting",
                        "startTime": "12:00:00+00:00",
                        "endTime": "13:00:00+00:00",
                        "day": "TUESDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        val template = templateEventsRepository.findById(templateId!!).get()
        assertThat(template.description).isEqualTo("Updated meeting")
        assertThat(template.weekDay).isEqualTo(WeekDay.TUESDAY)
        assertThat(template.startTime?.toLocalTime()).isEqualTo(OffsetTime.parse("12:00:00+00:00").toLocalTime())
        assertThat(template.endTime?.toLocalTime()).isEqualTo(OffsetTime.parse("13:00:00+00:00").toLocalTime())
    }

    @Test
    fun `upsertSingleEvent should create event in database`() {
        mockMvc.perform(
            post("/api/schedule/single")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Dentist",
                        "startTime": "09:00:00+00:00",
                        "endTime": "10:00:00+00:00",
                        "date": "2024-09-10"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        val events = currentEventsRepository.findAll().toList()
        assertThat(events).hasSize(1)
        val event = events.first()
        assertThat(event.description).isEqualTo("Dentist")
        assertThat(event.date).isEqualTo(LocalDate.of(2024, 9, 10))
        assertThat(event.startTime?.toLocalTime()).isEqualTo(OffsetTime.parse("09:00:00+00:00").toLocalTime())
        assertThat(event.endTime?.toLocalTime()).isEqualTo(OffsetTime.parse("10:00:00+00:00").toLocalTime())
        assertThat(event.user?.id.toString()).isEqualTo(userId)
        assertThat(event.template).isNull()
    }

    @Test
    fun `upsertSingleEvent should update existing event in database`() {
        val event = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.of(2024, 9, 10)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        currentEventsRepository.save(event)

        mockMvc.perform(
            post("/api/schedule/single")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "id": "${event.id}",
                        "description": "Updated meeting",
                        "startTime": "12:00:00+00:00",
                        "endTime": "13:00:00+00:00",
                        "date": "2024-09-11"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        val updated = currentEventsRepository.findById(event.id!!).get()
        assertThat(updated.description).isEqualTo("Updated meeting")
        assertThat(updated.date).isEqualTo(LocalDate.of(2024, 9, 11))
        assertThat(updated.startTime?.toLocalTime()).isEqualTo(OffsetTime.parse("12:00:00+00:00").toLocalTime())
        assertThat(updated.endTime?.toLocalTime()).isEqualTo(OffsetTime.parse("13:00:00+00:00").toLocalTime())
    }

    @Test
    fun `getAllEvents should return events for user from database`() {
        val event = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.of(2024, 9, 10)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        currentEventsRepository.save(event)

        mockMvc.perform(get("/api/schedule").param("userId", userId))
            .andExpect(status().isOk)
            .andExpect(content().json("""[{ "id": "${event.id}", "description": "Meeting" }]"""))
    }

    @Test
    fun `getAllTemplates should return templates for user from database`() {
        val template = EventsTemplateEntity().apply {
            id = UUID.randomUUID()
            description = "Weekly sync"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            weekDay = WeekDay.MONDAY
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        templateEventsRepository.save(template)

        mockMvc.perform(get("/api/schedule/template").param("userId", userId))
            .andExpect(status().isOk)
            .andExpect(content().json("""[{ "id": "${template.id}", "day": "MONDAY" }]"""))
    }

    @Test
    fun `deleteTemplate should delete template and related events from database`() {
        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Weekly meeting",
                        "startTime": "10:00:00+00:00",
                        "endTime": "11:00:00+00:00",
                        "day": "MONDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        assertThat(currentEventsRepository.findAll()).isNotEmpty

        val templateId = templateEventsRepository.findAll().first().id.toString()

        mockMvc.perform(delete("/api/schedule").param("templateId", templateId))
            .andExpect(status().isOk)

        assertThat(templateEventsRepository.findAll()).isEmpty()
        assertThat(currentEventsRepository.findAll()).isEmpty()
    }

    @Test
    fun `deleteSingleEvent should delete event and its notes from database`() {
        val event = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.of(2024, 9, 10)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        currentEventsRepository.save(event)

        val note = NoteEntity().apply {
            id = UUID.randomUUID()
            content = "Remember to call"
            flag = true
            this.event = event
        }
        noteRepository.save(note)

        mockMvc.perform(delete("/api/schedule/single").param("eventId", event.id.toString()))
            .andExpect(status().isOk)

        assertThat(currentEventsRepository.findAll()).isEmpty()
        assertThat(noteRepository.findAll()).isEmpty()
    }

    @Test
    fun `upsertNote should create note in database`() {
        val event = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.of(2024, 9, 10)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        currentEventsRepository.save(event)

        mockMvc.perform(
            post("/api/schedule/note")
                .param("eventId", event.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "Remember to call", "flag": true }""")
        )
            .andExpect(status().isOk)

        val notes = noteRepository.findAll().toList()
        assertThat(notes).hasSize(1)
        val note = notes.first()
        assertThat(note.content).isEqualTo("Remember to call")
        assertThat(note.flag).isTrue()
        assertThat(note.event?.id).isEqualTo(event.id)
    }

    @Test
    fun `upsertNote should update existing note in database`() {
        val event = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.of(2024, 9, 10)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        currentEventsRepository.save(event)

        val note = NoteEntity().apply {
            id = UUID.randomUUID()
            content = "Old content"
            flag = false
            this.event = event
        }
        noteRepository.save(note)

        mockMvc.perform(
            post("/api/schedule/note")
                .param("eventId", event.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "id": "${note.id}", "content": "Updated content", "flag": true }""")
        )
            .andExpect(status().isOk)

        val updated = noteRepository.findById(note.id!!).get()
        assertThat(updated.content).isEqualTo("Updated content")
        assertThat(updated.flag).isTrue()
    }

    @Test
    fun `deleteNote should delete note from database`() {
        val event = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.of(2024, 9, 10)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        currentEventsRepository.save(event)

        val note = NoteEntity().apply {
            id = UUID.randomUUID()
            content = "Remember to call"
            flag = true
            this.event = event
        }
        noteRepository.save(note)

        mockMvc.perform(delete("/api/schedule/note").param("noteId", note.id.toString()))
            .andExpect(status().isOk)

        assertThat(noteRepository.findAll()).isEmpty()
        assertThat(currentEventsRepository.findAll()).hasSize(1)
    }

    @Test
    fun `forceUpdate should remove old events and create new ones in database`() {
        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Weekly meeting",
                        "startTime": "10:00:00+00:00",
                        "endTime": "11:00:00+00:00",
                        "day": "SUNDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        val oldEvents = currentEventsRepository.findAll().toList()
        assertThat(oldEvents).isNotEmpty

        oldEvents.forEach { event ->
            event.date = LocalDate.now().minusMonths(3)
            currentEventsRepository.save(event)
        }

        mockMvc.perform(post("/api/schedule/force/update"))
            .andExpect(status().isOk)

        val newEvents = currentEventsRepository.findAll().toList()
        assertThat(newEvents).hasSize(1)
        assertThat(newEvents.first().description).isEqualTo("Weekly meeting")
        assertThat(newEvents.first().date).isAfter(LocalDate.now())
    }

    @Test
    fun `forceCreateNew should create future events for 4 weeks in database`() {
        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Weekly meeting",
                        "startTime": "10:00:00+00:00",
                        "endTime": "11:00:00+00:00",
                        "day": "SUNDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        currentEventsRepository.deleteAll()

        mockMvc.perform(post("/api/schedule/force/create_new"))
            .andExpect(status().isOk)

        val events = currentEventsRepository.findAll().toList()
        val todayOrdinal = LocalDate.now().dayOfWeek.value - 1
        val extraForCurrentWeek = if (WeekDay.SUNDAY.ordinal > todayOrdinal) 1 else 0
        assertThat(events).hasSize(4 + extraForCurrentWeek)
        events.forEach { event ->
            assertThat(event.description).isEqualTo("Weekly meeting")
            assertThat(event.date).isAfterOrEqualTo(LocalDate.now())
        }
    }

    @Test
    fun `forceCreateNewForNWeeks should create future events for N weeks in database`() {
        mockMvc.perform(
            post("/api/schedule")
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Weekly meeting",
                        "startTime": "10:00:00+00:00",
                        "endTime": "11:00:00+00:00",
                        "day": "SUNDAY"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)

        currentEventsRepository.deleteAll()

        mockMvc.perform(post("/api/schedule/force/create_new_n").param("weeks", "2"))
            .andExpect(status().isOk)

        val events = currentEventsRepository.findAll().toList()
        val todayOrdinal = LocalDate.now().dayOfWeek.value - 1
        val extraForCurrentWeek = if (WeekDay.SUNDAY.ordinal > todayOrdinal) 1 else 0
        assertThat(events).hasSize(2 + extraForCurrentWeek)
    }

    @Test
    fun `deleteFutureEvents should delete future events from database`() {
        val futureEvent = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Future"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.now().plusDays(1)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        val pastEvent = EventsCurrentEntity().apply {
            id = UUID.randomUUID()
            description = "Past"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            date = LocalDate.now().minusDays(1)
            user = userRepository.findById(UUID.fromString(userId)).get()
        }
        currentEventsRepository.saveAll(listOf(futureEvent, pastEvent))

        mockMvc.perform(delete("/api/schedule/force/delete_future"))
            .andExpect(status().isOk)

        val remaining = currentEventsRepository.findAll().toList()
        assertThat(remaining).hasSize(1)
        assertThat(remaining.first().description).isEqualTo("Past")
    }
}
