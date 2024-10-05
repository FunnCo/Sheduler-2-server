package com.funnco.schedulerbackend2.db.entity

import com.funnco.schedulerbackend2.dto.UserDTO
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "profile")
open class UserEntity {
    @Id
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    open var name: String? = null

    @OneToMany(mappedBy = "user")
    open var eventsCurrents: MutableSet<EventsCurrentEntity> = mutableSetOf()

    @OneToMany(mappedBy = "user")
    open var eventsTemplates: MutableSet<EventsTemplateEntity> = mutableSetOf()

    fun convertToDTO(): UserDTO{
        return UserDTO(id.toString(), name!!);
    }
}