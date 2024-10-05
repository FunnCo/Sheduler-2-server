package com.funnco.schedulerbackend2.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class UserDTO (
    var id: String? = null,
    var name: String? = null
) {
    @JsonIgnore
    fun isValid(): Boolean{
        return !name.isNullOrBlank()
    }
}