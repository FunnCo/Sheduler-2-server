package com.funnco.schedulerbackend2.db.repository

import com.funnco.schedulerbackend2.db.entity.EventsTemplateEntity
import com.funnco.schedulerbackend2.db.entity.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TemplateEventsRepository: CrudRepository<EventsTemplateEntity, UUID> {
    fun findAllByUserId(userId: UUID): List<EventsTemplateEntity>;
}