package com.funnco.schedulerbackend2.resource

import com.funnco.schedulerbackend2.dto.UserDTO
import com.funnco.schedulerbackend2.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserResource::class)
class UserResourceTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var userService: UserService

    @Test
    fun `getAllUsers should return list of users`() {
        every { userService.getAllUsers() } returns listOf(
            UserDTO(id = "1", name = "Alice"),
            UserDTO(id = "2", name = "Bob")
        )

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(content().json("""[{ "id": "1", "name": "Alice" }, { "id": "2", "name": "Bob" }]"""))
    }

    @Test
    fun `addNewUser should accept valid user`() {
        every { userService.addNewUser(any()) } just runs

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "name": "Alice" }""")
        )
            .andExpect(status().isOk)

        verify { userService.addNewUser(match { it.name == "Alice" }) }
    }
}
