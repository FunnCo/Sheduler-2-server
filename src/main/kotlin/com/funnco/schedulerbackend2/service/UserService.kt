package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.UserEntity
import com.funnco.schedulerbackend2.db.repository.UserRepository
import com.funnco.schedulerbackend2.dto.UserDTO
import com.funnco.schedulerbackend2.utils.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*


@Service
class UserService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var userRepository: UserRepository

    fun getAllUsers(): List<UserDTO> {
        return userRepository.findAll().map { it.convertToDTO() }.toList()
    }

    fun addNewUser(userDTO: UserDTO) {
        if (getUserEntryFromDTO(userDTO) != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User with such name already exists")
        }

        try{
            val newUserEntity = UserEntity()
            newUserEntity.name = userDTO.name;
            newUserEntity.id = UUID.randomUUID();
            userRepository.save(newUserEntity);
        } catch (e: Exception){
            log.error("BUG! Exception occured: ${e.message}", e)
            ExceptionUtils.handleRequestException(e)
        }

    }

    private fun getUserEntryFromDTO(userDTO: UserDTO): UserEntity? {
        if (!userDTO.isValid()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Name can't be null or blank")
        }
        return if (userDTO.id == null) {
            userRepository.findByName(userDTO.name).orElse(null)
        } else {
            userRepository.findById(UUID.fromString(userDTO.id!!)).orElse(null)
        }
    }

    fun getUserById(id: String) : UserEntity{
        val user = userRepository.findById(UUID.fromString(id)).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No such user found")
        return user;
    }
}