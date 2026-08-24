package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.EventsTemplateEntity
import com.funnco.schedulerbackend2.db.entity.UserEntity
import com.funnco.schedulerbackend2.db.model.WeekDay
import com.funnco.schedulerbackend2.db.repository.TemplateEventsRepository
import com.funnco.schedulerbackend2.dto.TemplateEventDTO
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
import java.time.OffsetTime
import java.util.*

@ExtendWith(MockKExtension::class)
class TemplateEventsServiceTest {

    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var templateEventsRepository: TemplateEventsRepository

    @MockK
    lateinit var currentEventsService: CurrentEventsService

    @InjectMockKs
    lateinit var templateEventsService: TemplateEventsService

    private val userId = UUID.randomUUID().toString()
    private val user = UserEntity().apply {
        id = UUID.fromString(userId)
        name = "Alice"
    }

    @Test
    fun `addEntryForUser should create template and generate current events`() {
        every { userService.getUserById(userId) } returns user
        val capturedTemplate = slot<EventsTemplateEntity>()
        every { templateEventsRepository.save(capture(capturedTemplate)) } answers { capturedTemplate.captured }
        every { currentEventsService.addByTemplate(any()) } just runs

        val dto = TemplateEventDTO(
            description = "Weekly meeting",
            startTime = OffsetTime.parse("10:00:00+00:00"),
            endTime = OffsetTime.parse("11:00:00+00:00"),
            day = "MONDAY"
        )

        templateEventsService.addEntryForUser(userId, dto)

        assertThat(capturedTemplate.captured.description).isEqualTo("Weekly meeting")
        assertThat(capturedTemplate.captured.weekDay).isEqualTo(WeekDay.MONDAY)
        assertThat(capturedTemplate.captured.user).isEqualTo(user)
        verify { currentEventsService.addByTemplate(capturedTemplate.captured) }
    }

    @Test
    fun `getAllTemplatesForUser should return list of DTOs`() {
        val template = EventsTemplateEntity().apply {
            id = UUID.randomUUID()
            description = "Meeting"
            startTime = OffsetTime.parse("10:00:00+00:00")
            endTime = OffsetTime.parse("11:00:00+00:00")
            weekDay = WeekDay.TUESDAY
        }
        every { templateEventsRepository.findAllByUserId(UUID.fromString(userId)) } returns listOf(template)

        val result = templateEventsService.getAllTemplatesForUser(userId)

        assertThat(result).hasSize(1)
        assertThat(result[0].description).isEqualTo("Meeting")
        assertThat(result[0].day).isEqualTo("TUESDAY")
    }

    @Test
    fun `deleteTemplate should delete template and related current events`() {
        val templateId = UUID.randomUUID()
        val template = EventsTemplateEntity().apply { id = templateId }
        every { templateEventsRepository.findById(templateId) } returns Optional.of(template)
        every { currentEventsService.deleteByTemplate(template) } just runs
        every { templateEventsRepository.delete(template) } just runs

        templateEventsService.deleteTemplate(templateId.toString())

        verify { currentEventsService.deleteByTemplate(template) }
        verify { templateEventsRepository.delete(template) }
    }

    @Test
    fun `deleteTemplate should throw NOT_FOUND when template does not exist`() {
        val templateId = UUID.randomUUID()
        every { templateEventsRepository.findById(templateId) } returns Optional.empty()

        val exception = assertThrows<ResponseStatusException> {
            templateEventsService.deleteTemplate(templateId.toString())
        }

        assertThat(exception.statusCode.value()).isEqualTo(404)
    }

    @Test
    fun `updateEntry should update template and related current events`() {
        val templateId = UUID.randomUUID()
        val template = EventsTemplateEntity().apply {
            id = templateId
            description = "Old"
            weekDay = WeekDay.MONDAY
        }
        every { templateEventsRepository.findById(templateId) } returns Optional.of(template)
        every { currentEventsService.updateByTemplate(template) } just runs
        val captured = slot<EventsTemplateEntity>()
        every { templateEventsRepository.save(capture(captured)) } answers { captured.captured }

        val dto = TemplateEventDTO(
            id = templateId.toString(),
            description = "New",
            startTime = OffsetTime.parse("12:00:00+00:00"),
            endTime = OffsetTime.parse("13:00:00+00:00"),
            day = "FRIDAY"
        )

        templateEventsService.updateEntry(dto)

        assertThat(captured.captured.description).isEqualTo("New")
        assertThat(captured.captured.weekDay).isEqualTo(WeekDay.FRIDAY)
        verify { currentEventsService.updateByTemplate(template) }
    }
}
