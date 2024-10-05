package com.funnco.schedulerbackend2.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.funnco.schedulerbackend2.db.entity.NoteEntity

data class NoteDTO(
    var id: String? = null,
    var content: String? = null,
    var flag: Boolean? = false
) {
    constructor(entity: NoteEntity) : this(
        id = entity.id.toString(),
        content = entity.content,
        flag = entity.flag
    )
}