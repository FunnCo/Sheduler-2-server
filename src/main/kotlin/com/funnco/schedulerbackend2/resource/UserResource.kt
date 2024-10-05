package com.funnco.schedulerbackend2.resource

import com.funnco.schedulerbackend2.dto.UserDTO
import com.funnco.schedulerbackend2.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserResource {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var userService: UserService

    @GetMapping
    fun getAllUsers(): List<UserDTO> {
        return userService.getAllUsers()
    }

    @PostMapping
    fun addNewUser(@RequestBody userDTO: UserDTO) {
        log.info("Received task for creation of new user: $userDTO")
        userService.addNewUser(userDTO)
    }
}