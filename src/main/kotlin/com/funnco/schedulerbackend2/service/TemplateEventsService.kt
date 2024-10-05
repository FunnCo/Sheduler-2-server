package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.EventsTemplateEntity
import com.funnco.schedulerbackend2.db.model.WeekDay
import com.funnco.schedulerbackend2.db.repository.TemplateEventsRepository
import com.funnco.schedulerbackend2.dto.TemplateEventDTO
import com.funnco.schedulerbackend2.utils.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class TemplateEventsService {

    private val className = this::class.java.simpleName
    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var templateEventsRepository: TemplateEventsRepository

    @Autowired
    private lateinit var currentEventsService: CurrentEventsService

    @Transactional
    fun addEntryForUser(userId: String, dto: TemplateEventDTO) {
        try {
            log.info("[$className] Start add template for user $userId")
            val user = userService.getUserById(userId);
            var templateEntity = EventsTemplateEntity()
            templateEntity.id = UUID.randomUUID()
            templateEntity.user = user
            templateEntity.description = dto.description
            templateEntity.weekDay = WeekDay.valueOf(dto.day!!)
            templateEntity.startTime = dto.startTime
            templateEntity.endTime = dto.endTime

            templateEntity = templateEventsRepository.save(templateEntity)
            currentEventsService.addByTemplate(templateEntity)
            log.info("[$className] End add template for user $userId with templateId ${templateEntity.id.toString()}")
        } catch (e: Exception) {
            log.error("[$className] Exception while adding entry for user $userId", e)
            ExceptionUtils.handleRequestException(e)
        }

    }

    fun getAllTemplatesForUser(userId: String): List<TemplateEventDTO> {
        try {
            log.info("[$className] Starting get all templates for user $userId")
            val templates = templateEventsRepository.findAllByUserId(UUID.fromString(userId))

            val templatesDto = templates.stream().map { templateEntity ->
                TemplateEventDTO(
                    templateEntity.id.toString(),
                    templateEntity.description,
                    templateEntity.startTime,
                    templateEntity.endTime,
                    templateEntity.weekDay!!.name
                )
            }.toList()
            log.info("[$className] Found ${templatesDto.size} templates")
            return templatesDto;

        } catch (e: Exception) {
            log.error("[$className] Exception while getting all templates for user $userId", e)
            ExceptionUtils.handleRequestException(e)
            return emptyList()
        }
    }

    @Transactional
    fun deleteTemplate(templateId: String) {
        try {
            log.info("[$className] Start delete entry $templateId")
            val templateEntity = templateEventsRepository.findById(UUID.fromString(templateId)).orElse(null)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "template with such id is not found")
            currentEventsService.deleteByTemplate(templateEntity)
            templateEventsRepository.delete(templateEntity)
            log.info("[$className] End delete entry $templateId")
        } catch (e: Exception) {
            log.error("[$className] Exception while removing entry $templateId", e)
            ExceptionUtils.handleRequestException(e)
        }
    }

    @Transactional
    fun updateEntry(dto: TemplateEventDTO) {
        val templateId = dto.id.toString()
        try {
            log.info("[$className] Start update template $templateId")
            val templateEntity = templateEventsRepository.findById(UUID.fromString(templateId)).orElse(null)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "template with such id is not found")

            templateEntity.description = dto.description
            templateEntity.weekDay = WeekDay.valueOf(dto.day!!)
            templateEntity.startTime = dto.startTime
            templateEntity.endTime = dto.endTime

            currentEventsService.updateByTemplate(templateEntity)
            templateEventsRepository.save(templateEntity)
            log.info("[$className] End update template $templateId")

        } catch (e: Exception) {
            log.error("[$className] Exception while updating entry $templateId", e)
            ExceptionUtils.handleRequestException(e)
        }
    }
}