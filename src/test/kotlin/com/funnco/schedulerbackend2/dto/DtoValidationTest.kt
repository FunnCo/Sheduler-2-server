package com.funnco.schedulerbackend2.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetTime

class DtoValidationTest {

    @Test
    fun `EventDTO is valid when start time is before end time`() {
        val dto = EventDTO(
            startTime = OffsetTime.parse("10:00:00+00:00"),
            endTime = OffsetTime.parse("11:00:00+00:00"),
            date = LocalDate.of(2024, 9, 10)
        )

        assertThat(dto.isValid()).isTrue()
    }

    @Test
    fun `EventDTO is invalid when start time is after end time`() {
        val dto = EventDTO(
            startTime = OffsetTime.parse("12:00:00+00:00"),
            endTime = OffsetTime.parse("11:00:00+00:00")
        )

        assertThat(dto.isValid()).isFalse()
    }

    @Test
    fun `EventDTO is invalid when times are null`() {
        val dto = EventDTO()

        assertThat(dto.isValid()).isFalse()
    }

    @Test
    fun `TemplateEventDTO is valid with correct day and time`() {
        val dto = TemplateEventDTO(
            description = "Meeting",
            startTime = OffsetTime.parse("10:00:00+00:00"),
            endTime = OffsetTime.parse("11:00:00+00:00"),
            day = "WEDNESDAY"
        )

        assertThat(dto.isValid()).isTrue()
    }

    @Test
    fun `TemplateEventDTO is invalid with blank description`() {
        val dto = TemplateEventDTO(
            description = "   ",
            startTime = OffsetTime.parse("10:00:00+00:00"),
            endTime = OffsetTime.parse("11:00:00+00:00"),
            day = "MONDAY"
        )

        assertThat(dto.isValid()).isFalse()
    }

    @Test
    fun `TemplateEventDTO is invalid with unknown day`() {
        val dto = TemplateEventDTO(
            description = "Meeting",
            startTime = OffsetTime.parse("10:00:00+00:00"),
            endTime = OffsetTime.parse("11:00:00+00:00"),
            day = "FUNDAY"
        )

        assertThat(dto.isValid()).isFalse()
    }

    @Test
    fun `TemplateEventDTO is invalid when start time is after end time`() {
        val dto = TemplateEventDTO(
            description = "Meeting",
            startTime = OffsetTime.parse("12:00:00+00:00"),
            endTime = OffsetTime.parse("11:00:00+00:00"),
            day = "MONDAY"
        )

        assertThat(dto.isValid()).isFalse()
    }

    @Test
    fun `UserDTO is valid with non-blank name`() {
        val dto = UserDTO(name = "Alice")

        assertThat(dto.isValid()).isTrue()
    }

    @Test
    fun `UserDTO is invalid with blank name`() {
        val dto = UserDTO(name = "   ")

        assertThat(dto.isValid()).isFalse()
    }

    @Test
    fun `UserDTO is invalid when name is null`() {
        val dto = UserDTO()

        assertThat(dto.isValid()).isFalse()
    }
}
