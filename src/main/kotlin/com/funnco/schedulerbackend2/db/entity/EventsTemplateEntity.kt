package com.funnco.schedulerbackend2.db.entity

import com.funnco.schedulerbackend2.db.model.WeekDay
import jakarta.persistence.*
import org.hibernate.annotations.ColumnTransformer
import java.time.OffsetTime
import java.util.*

@Entity
@Table(name = "events_template")
open class EventsTemplateEntity {
    @Id
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    open var description: String? = null

    @Column(name = "start_time", nullable = false)
    open var startTime: OffsetTime? = null

    @Column(name = "end_time", nullable = false)
    open var endTime: OffsetTime? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: UserEntity? = null

    @Enumerated(EnumType.STRING)
    open var weekDay: WeekDay? = null
}