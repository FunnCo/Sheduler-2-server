package com.funnco.schedulerbackend2.db.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.util.*

@Entity
@Table(name = "note")
open class NoteEntity {
    @Id
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    open var content: String? = null

    @ColumnDefault("false")
    @Column(name = "flag", nullable = false)
    open var flag: Boolean? = false

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    open var event: EventsCurrentEntity? = null
}