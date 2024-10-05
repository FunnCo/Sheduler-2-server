package com.funnco.schedulerbackend2.db.entity

import com.funnco.schedulerbackend2.dto.TemplateEventDTO
import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetTime
import java.util.*

@Entity
@Table(name = "events_current")
open class EventsCurrentEntity {
    @Id
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    open var description: String? = null

    @Column(name = "start_time", nullable = false)
    open var startTime: OffsetTime? = null

    @Column(name = "end_time", nullable = false)
    open var endTime: OffsetTime? = null

    @Column(name = "date", nullable = false)
    open var date: LocalDate? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = true)
    open var template: EventsTemplateEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    open var user: UserEntity? = null

    @OneToMany(mappedBy = "event")
    open var noteEntities: MutableSet<NoteEntity> = mutableSetOf()
}