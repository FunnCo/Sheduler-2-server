package com.funnco.schedulerbackend2.service

import com.funnco.schedulerbackend2.db.entity.UserEntity
import com.funnco.schedulerbackend2.db.repository.UserRepository
import com.funnco.schedulerbackend2.dto.UserDTO
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.util.*

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var userService: UserService

    @Test
    fun `getAllUsers should return list of user DTOs`() {
        val user1 = UserEntity().apply {
            id = UUID.randomUUID()
            name = "Alice"
        }
        val user2 = UserEntity().apply {
            id = UUID.randomUUID()
            name = "Bob"
        }
        every { userRepository.findAll() } returns listOf(user1, user2)

        val result = userService.getAllUsers()

        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Alice")
        assertThat(result[1].name).isEqualTo("Bob")
    }

    @Test
    fun `getAllUsers should return empty list when no users`() {
        every { userRepository.findAll() } returns emptyList()

        val result = userService.getAllUsers()

        assertThat(result).isEmpty()
    }

    @Test
    fun `addNewUser should save user when name does not exist`() {
        every { userRepository.findByName("Alice") } returns Optional.empty()
        val captured = slot<UserEntity>()
        every { userRepository.save(capture(captured)) } answers { captured.captured }

        userService.addNewUser(UserDTO(name = "Alice"))

        assertThat(captured.captured.name).isEqualTo("Alice")
        assertThat(captured.captured.id).isNotNull()
    }

    @Test
    fun `addNewUser should throw BAD_REQUEST when user already exists`() {
        val existing = UserEntity().apply {
            id = UUID.randomUUID()
            name = "Alice"
        }
        every { userRepository.findByName("Alice") } returns Optional.of(existing)

        val exception = assertThrows<ResponseStatusException> {
            userService.addNewUser(UserDTO(name = "Alice"))
        }

        assertThat(exception.statusCode.value()).isEqualTo(400)
    }

    @Test
    fun `addNewUser should throw BAD_REQUEST when name is blank`() {
        val exception = assertThrows<ResponseStatusException> {
            userService.addNewUser(UserDTO(name = "   "))
        }

        assertThat(exception.statusCode.value()).isEqualTo(400)
    }

    @Test
    fun `getUserById should return user when found`() {
        val id = UUID.randomUUID()
        val user = UserEntity().apply {
            this.id = id
            name = "Alice"
        }
        every { userRepository.findById(id) } returns Optional.of(user)

        val result = userService.getUserById(id.toString())

        assertThat(result.name).isEqualTo("Alice")
    }

    @Test
    fun `getUserById should throw BAD_REQUEST when user not found`() {
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()

        val exception = assertThrows<ResponseStatusException> {
            userService.getUserById(id.toString())
        }

        assertThat(exception.statusCode.value()).isEqualTo(400)
    }
}
