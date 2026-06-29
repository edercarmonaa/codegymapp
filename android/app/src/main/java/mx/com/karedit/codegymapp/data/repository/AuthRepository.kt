package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.core.session.SessionManager
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.LoginRequestDto
import mx.com.karedit.codegymapp.domain.model.User

class AuthRepository(
    private val api: CodeGymApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(username: String, password: String): Result<User> = runCatching {
        val response = api.login(LoginRequestDto(username = username, password = password))
        val token = response.token
        val user = response.user
        if (!response.ok || token.isNullOrBlank() || user == null) {
            error(response.message ?: "Usuario o contraseña incorrectos.")
        }
        sessionManager.saveToken(token)
        user.toDomain()
    }

    suspend fun me(): Result<User> = runCatching {
        val response = api.me()
        val user = response.user
        if (!response.ok || user == null) {
            error(response.message ?: "No se pudo cargar la sesión.")
        }
        user.toDomain()
    }

    fun hasToken(): Boolean = !sessionManager.token().isNullOrBlank()

    fun logoutAndClear() {
        sessionManager.clearSession()
    }
}

private fun mx.com.karedit.codegymapp.data.remote.dto.UserDto.toDomain(): User =
    User(id = id, username = username, name = name, email = email)
