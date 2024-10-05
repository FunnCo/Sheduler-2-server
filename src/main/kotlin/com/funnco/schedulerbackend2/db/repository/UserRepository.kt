package com.funnco.schedulerbackend2.db.repository

import com.funnco.schedulerbackend2.db.entity.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository: CrudRepository<UserEntity, UUID> {

    fun findByName(name: String?): Optional<UserEntity>
}