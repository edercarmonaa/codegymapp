package mx.com.karedit.codegymapp.data.remote.api

import mx.com.karedit.codegymapp.data.remote.dto.LoginRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.LoginResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MeResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileTodayResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CodeGymApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("api/me")
    suspend fun me(): MeResponseDto

    @GET("api/mobile/today")
    suspend fun mobileToday(): MobileTodayResponseDto
}
