package com.funnco.schedulerbackend2.utils

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ExceptionUtils {

    fun handleRequestException(exception: Exception) {
        if (exception is ResponseStatusException) {
            throw exception
        } else {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Bug occurred. Please message creator about it"
            )
        }
    }
}