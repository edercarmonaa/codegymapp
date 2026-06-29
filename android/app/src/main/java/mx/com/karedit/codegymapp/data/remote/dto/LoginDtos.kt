package mx.com.karedit.codegymapp.data.remote.dto

import com.squareup.moshi.Json

data class LoginRequestDto(
    val username: String,
    val password: String
)

data class LoginResponseDto(
    val ok: Boolean,
    val token: String?,
    @Json(name = "expires_in") val expiresIn: Int?,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "refresh_expires_in") val refreshExpiresIn: Int? = null,
    val user: UserDto?,
    val message: String?
)

data class RefreshSessionRequestDto(
    @Json(name = "refresh_token") val refreshToken: String
)

data class MeResponseDto(
    val ok: Boolean,
    val user: UserDto?,
    val message: String?
)

data class UserDto(
    val id: Int,
    val username: String,
    val name: String,
    val email: String,
    @Json(name = "preferred_theme") val preferredTheme: String? = null,
    @Json(name = "last_login_at") val lastLoginAt: String? = null
)
