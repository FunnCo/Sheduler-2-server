package com.funnco.schedulerbackend2.db.repository

import com.funnco.schedulerbackend2.db.entity.EventsCurrentEntity
import com.funnco.schedulerbackend2.db.entity.EventsTemplateEntity
import com.funnco.schedulerbackend2.db.entity.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface CurrentEventsRepository : CrudRepository<EventsCurrentEntity, UUID>{
    fun getEventsCurrentsByUser(user: UserEntity): List<EventsCurrentEntity>
    fun getEventsCurrentEntitiesByTemplate(template: EventsTemplateEntity): List<EventsCurrentEntity>
    fun getEventsCurrentEntitiesByDateBefore(date: LocalDate): List<EventsCurrentEntity>
    fun deleteAllByDateBefore(date: LocalDate)
    fun deleteAllByTemplate(template: EventsTemplateEntity)
}