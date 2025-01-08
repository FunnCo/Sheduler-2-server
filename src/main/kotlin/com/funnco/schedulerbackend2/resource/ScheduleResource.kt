package com.funnco.schedulerbackend2.resource

import com.funnco.schedulerbackend2.dto.EventDTO
import com.funnco.schedulerbackend2.dto.NoteDTO
import com.funnco.schedulerbackend2.dto.TemplateEventDTO
import com.funnco.schedulerbackend2.service.CurrentEventsService
import com.funnco.schedulerbackend2.service.NoteService
import com.funnco.schedulerbackend2.service.TemplateEventsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


@RestController
@RequestMapping("/api/schedule")
class ScheduleResource {

    @Autowired
    lateinit var currentEventsService: CurrentEventsService

    @Autowired
    lateinit var templateEventsService: TemplateEventsService

    @Autowired
    lateinit var noteService: NoteService

    @PostMapping
    fun upsertTemplate(
        @RequestParam userId: String,
        @RequestBody dto: TemplateEventDTO
    ) {
        if (!dto.isValid()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "event data DTO is wrong")
        }
        if (dto.id == null) {
            templateEventsService.addEntryForUser(userId, dto)
        } else {
            templateEventsService.updateEntry(dto)
        }
    }

    @PostMapping("/single")
    fun upsertSingleEvent(
        @RequestParam userId: String,
        @RequestBody dto: EventDTO
    ) {
        if (!dto.isValid()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "event data DTO is wrong")
        }
        if (dto.id == null) {
            currentEventsService.addSingleEvent(userId, dto)
        } else {
            currentEventsService.updateSingleEvent(dto)
        }
    }

    @GetMapping
    fun getAllEvents(
        @RequestParam userId: String,
    ): List<EventDTO> {
        return currentEventsService.getEventsForUser(userId)
    }

    @GetMapping("/template")
    fun getAllTemplates(
        @RequestParam userId: String,
    ): List<TemplateEventDTO> {
        return templateEventsService.getAllTemplatesForUser(userId)
    }

    @DeleteMapping
    fun deleteTemplate(@RequestParam templateId: String) {
        templateEventsService.deleteTemplate(templateId)
    }

    @DeleteMapping("/single")
    fun deleteSingleEvent(@RequestParam eventId: String) {
        currentEventsService.deleteSingleEvent(eventId)
    }

    @PostMapping("/note")
    fun upsertNote(@RequestParam eventId: String, @RequestBody dto: NoteDTO) {
        if (dto.id == null) {
            noteService.addNote(dto, eventId)
        } else {
            noteService.updateNote(dto)
        }
    }

    @DeleteMapping("/note")
    fun deleteNote(@RequestParam noteId: String) {
        noteService.deleteNote(noteId)
    }

    @PostMapping("/force/update")
    fun forceUpdate(){
        currentEventsService.updateEvents()
    }

    @PostMapping("/force/create_new")
    fun forceUpdateNew(){
        currentEventsService.forceCreateFutureEventsByTemplate(4)
    }

    @DeleteMapping("/force/delete_future")
    fun deleteFutureEvents(){
        currentEventsService.deleteAllFutureEvents()
    }
}