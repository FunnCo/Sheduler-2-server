package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.EventsCurrentEntity
import com.funnco.schedulerbackend2.db.entity.EventsTemplateEntity
import com.funnco.schedulerbackend2.db.repository.CurrentEventsRepository
import com.funnco.schedulerbackend2.db.repository.NoteRepository
import com.funnco.schedulerbackend2.db.repository.TemplateEventsRepository
import com.funnco.schedulerbackend2.dto.EventDTO
import com.funnco.schedulerbackend2.utils.DateUtils
import com.funnco.schedulerbackend2.utils.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class CurrentEventsService {

    @Autowired
    private lateinit var noteService: NoteService
    private val className = this::class.java.simpleName
    private val log = LoggerFactory.getLogger(className)

    @Autowired
    private lateinit var templateEventsRepository: TemplateEventsRepository

    @Autowired
    private lateinit var currentEventsRepository: CurrentEventsRepository

    @Autowired
    private lateinit var userService: UserService

    /*
    TODO: Checks, to be unable to create events at the time, that is already scheduled for this user.
     */

    fun addSingleEvent(userId: String, eventDto: EventDTO) {
        try {
            log.info("[$className] Start creating new single event for user: ${userId}")
            var newSingleEvent = EventsCurrentEntity()
            newSingleEvent.id = UUID.randomUUID()
            newSingleEvent.user = userService.getUserById(userId)
            newSingleEvent.date = eventDto.date
            newSingleEvent.startTime = eventDto.startTime
            newSingleEvent.endTime = eventDto.endTime
            newSingleEvent.description = eventDto.description
            newSingleEvent = currentEventsRepository.save(newSingleEvent)
            log.info("[$className] End create new event ${newSingleEvent.id.toString()} for user: ${userId}")
        } catch (any: Exception) {
            log.error("[$className] Error while creating new single event for user: $userId", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    fun updateSingleEvent(eventDto: EventDTO) {
        try {
            log.info("[$className] Start update single event ${eventDto.id}")
            val eventToUpdate = currentEventsRepository.findById(UUID.fromString(eventDto.id)).getOrNull()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "event with such id is not found")
            eventToUpdate.date = eventDto.date
            eventToUpdate.startTime = eventDto.startTime
            eventToUpdate.endTime = eventDto.endTime
            eventToUpdate.description = eventDto.description
            currentEventsRepository.save(eventToUpdate)
            log.info("[$className] End update event ${eventDto.id}")
        } catch (any: Exception) {
            log.error("[$className] Error while updating event ${eventDto.id}", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    fun deleteSingleEvent(eventId: String) {
        try {
            log.info("[$className] Start delete single event $eventId")
            noteService.deleteAllEventNotes(eventId)
            currentEventsRepository.deleteById(UUID.fromString(eventId))
            log.info("[$className] End delete event $eventId")
        } catch (any: Exception) {
            log.error("[$className] Error while deleting event $eventId", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    fun getEventsForUser(userId: String): List<EventDTO> {
        try {
            val user = userService.getUserById(userId)
            val events = currentEventsRepository.getEventsCurrentsByUser(user);
            return events.stream()
                .map { event -> EventDTO(event) }
                .toList()
        } catch (any: Exception) {
            ExceptionUtils.handleRequestException(any)
            return emptyList()
        }
    }

    @Transactional
    fun addByTemplate(template: EventsTemplateEntity) {
        try {
            log.info("[$className] Start creating new events by template: ${template.id}")
            var newEvents = mutableListOf<EventsCurrentEntity>()
            for (i in 0..4) {
                newEvents.add(mapTemplateToEvent(template, i.toLong()))
            }
            val weekStart = DateUtils.getCurrentWeekStartDate();
            newEvents =
                newEvents.filter { it.date!!.isEqual(weekStart) || it.date!!.isAfter(weekStart) }.toMutableList()
            currentEventsRepository.saveAll(newEvents)
            log.info("[$className] End creating new events by template: ${template.id}")
        } catch (any: Exception) {
            log.error("[$className] Error while creating new events by template: ${template.id}", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    @Transactional
    fun deleteByTemplate(template: EventsTemplateEntity) {
        try {
            log.info("[$className] Start deleting current events by template: ${template.id}")
            val eventsToDelete = currentEventsRepository.getEventsCurrentEntitiesByTemplate(template)
            eventsToDelete.forEach {
                event -> noteService.deleteAllEventNotes(event.id.toString())
            }
            currentEventsRepository.deleteAll(eventsToDelete)
            log.info("[$className] End deleting current events by template: ${template.id}")
        } catch (any: Exception) {
            log.error("[$className] Error while deleting new events by template: ${template.id}", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    @Transactional
    fun forceCreateFutureEventsByTemplate(weeks: Int) {
        try {
            log.info("[$className] Start forcefully creating new notes from all templates (weeks: ${weeks})")
            val allTemplates = templateEventsRepository.findAll().toList()
            for (currentWeekOffset in 1..weeks) {
                val newEvents = allTemplates.stream()
                    .map { template -> mapTemplateToEvent(template, currentWeekOffset.toLong()) }
                    .toList()
                currentEventsRepository.saveAll(newEvents)
            }
            log.info("[$className] End forcefully creating new notes from all templates (weeks: ${weeks})")
        } catch (any: Exception) {
            log.error(
                "[$className] Error while forcefully creating new notes from all templates (weeks: ${weeks})",
                any
            )
            ExceptionUtils.handleRequestException(any)
        }
    }

    @Transactional
    fun updateByTemplate(template: EventsTemplateEntity) {
        try {
            log.info("[$className] Start updating current events by template: ${template.id}")
            val events = currentEventsRepository.getEventsCurrentEntitiesByTemplate(template)
            events.forEach { event ->
                event.endTime = template.endTime
                event.startTime = template.startTime
                event.description = template.description
                event.template = template
                val currentWeekDay = event.date!!.dayOfWeek.value - 1
                val differenceInDays = template.weekDay!!.ordinal - currentWeekDay
                event.date = event.date!!.plusDays(differenceInDays.toLong())
            }
            currentEventsRepository.saveAll(events)
            log.info("[$className] End updating current events by template: ${template.id}")
        } catch (any: Exception) {
            log.error("[$className] Error while updating events by template: ${template.id}", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    private fun mapTemplateToEvent(template: EventsTemplateEntity, weekOffset: Long): EventsCurrentEntity {
        val newEvent = EventsCurrentEntity()
        newEvent.id = UUID.randomUUID()
        newEvent.template = template
        newEvent.endTime = template.endTime
        newEvent.startTime = template.startTime
        newEvent.description = template.description
        newEvent.user = template.user
        val weekStartDay = DateUtils.getCurrentWeekStartDate()
        newEvent.date = weekStartDay // Находим нужный день недели
            .plusWeeks(weekOffset)
            .plusDays(template.weekDay!!.ordinal.toLong() + 1)
        return newEvent
    }

    @Transactional
    fun deleteAllFutureEvents() {
        try {
            log.info("[$className] Start forcefully deleting all future notes")
            val allFutureEvents = currentEventsRepository
                .findAll()
                .filter { it.date!!.isAfter(LocalDate.now()) }
            allFutureEvents.stream()
                .map { it.noteEntities.stream() }
                .flatMap { notes -> notes }
                .forEach { noteService.deleteNote(it.id!!.toString()) }
            allFutureEvents.forEach { currentEventsRepository.delete(it) }
            log.info("[$className] End forcefully deleting all future notes")
        } catch (any: Exception) {
            log.error("[$className] Error while deleting all future notes", any)
            ExceptionUtils.handleRequestException(any)
        }
    }

    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    fun updateEvents() {
        try {
            log.info("[$className] Begin weekly update of events")
            val oldEvents =
                currentEventsRepository.getEventsCurrentEntitiesByDateBefore((LocalDate.now().minusWeeks(5)))
            oldEvents.stream()
                .map { event -> event.noteEntities.stream() }
                .flatMap { notes -> notes }
                .forEach { note -> noteService.deleteNote(note.id!!.toString()) }
            oldEvents.forEach { event -> currentEventsRepository.delete(event) }
            val allTemplates = templateEventsRepository.findAll().toList()
            val newEvents = allTemplates.stream()
                .map { template -> mapTemplateToEvent(template, 5) }
                .toList()
            currentEventsRepository.saveAll(newEvents)
            log.info("[$className] End weekly update of events")
        } catch (any: Exception) {
            log.error("[$className] Error while weekly updating", any)
            throw any
        }
    }

}