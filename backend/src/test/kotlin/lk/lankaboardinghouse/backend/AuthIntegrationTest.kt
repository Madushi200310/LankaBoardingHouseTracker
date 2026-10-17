package lk.lankaboardinghouse.backend

import tools.jackson.databind.ObjectMapper
import lk.lankaboardinghouse.backend.dto.LoginRequestDto
import lk.lankaboardinghouse.backend.dto.RegisterRequestDto
import lk.lankaboardinghouse.backend.model.Role
import lk.lankaboardinghouse.backend.model.User
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder // Added this

    private val adminEmail = "admin-test@lankaboarding.lk"
    private val userEmail = "user-test@lankaboarding.lk"
    private val ownerEmail = "owner-test@lankaboarding.lk"
    private val password = "Password123!"

    @BeforeEach
    fun setup() {
        if (userRepository.findByEmail(adminEmail) == null) {
            // Create admin directly in DB because the API blocks admin registration
            val admin = User(
                fullName = "Admin Test",
                email = adminEmail,
                password = passwordEncoder.encode(password) ?: "",
                phoneNumber = "0771234567",
                role = Role.ADMIN
            )
            userRepository.save(admin)
        }
        if (userRepository.findByEmail(userEmail) == null) {
            registerUser("User Test", userEmail, Role.USER)
        }
        if (userRepository.findByEmail(ownerEmail) == null) {
            registerUser("Owner Test", ownerEmail, Role.OWNER)
        }
    }

    private fun registerUser(name: String, email: String, role: Role) {
        val dto = RegisterRequestDto(name, email, password, "0771234567", role)
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
    }

    private fun getToken(email: String): String {
        val loginDto = LoginRequestDto(email, password)
        val response = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        ).andReturn().response.contentAsString

        val jsonNode = objectMapper.readTree(response)
        return jsonNode["token"].asString()
    }

    // --- Security Checks ---

    @Test
    fun requestWithoutTokenReturns401() {
        mockMvc.perform(get("/api/boarding-houses/search?townId=1"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun userAccessingAdminEndpointReturns403() {
        val token = getToken(userEmail)
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun adminAccessingAdminEndpointReturns200() {
        val token = getToken(adminEmail)
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
    }

    @Test
    fun ownerAccessingOwnerEndpointReturns200() {
        val token = getToken(ownerEmail)
        val user = userRepository.findByEmail(ownerEmail)
        mockMvc.perform(
            get("/api/boarding-houses/owner/${user?.id}")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
    }

    // --- Token Invalidation ---

    @Test
    fun changingPasswordInvalidatesOldToken() {
        val token = getToken(userEmail)

        mockMvc.perform(get("/api/favourites").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)

        val changePasswordJson = """
            {
                "currentPassword": "$password",
                "newPassword": "NewPassword123!"
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/account/password")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson)
        ).andExpect(status().isNoContent)

        mockMvc.perform(get("/api/favourites").header("Authorization", "Bearer $token"))
            .andExpect(status().isUnauthorized)
    }
}