package mx.com.karedit.codegymapp.data.remote.api

import mx.com.karedit.codegymapp.data.remote.dto.LoginRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.LoginResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MeResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeDetailsRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengesResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileCreateOptionsResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileDeviceTokenRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileNotificationActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileNotificationsResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobilePlannedResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileRoutineCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileSummaryResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileTodayResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CodeGymApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("api/me")
    suspend fun me(): MeResponseDto

    @GET("api/mobile/summary")
    suspend fun mobileSummary(): MobileSummaryResponseDto

    @GET("api/mobile/notifications")
    suspend fun mobileNotifications(): MobileNotificationsResponseDto

    @POST("api/mobile/notifications/mark-read")
    suspend fun markNotificationRead(@Body request: MobileNotificationActionRequestDto): MobileActionResponseDto

    @POST("api/mobile/notifications/delete")
    suspend fun deleteNotification(@Body request: MobileNotificationActionRequestDto): MobileActionResponseDto

    @POST("api/mobile/device-token")
    suspend fun storeDeviceToken(@Body request: MobileDeviceTokenRequestDto): MobileActionResponseDto

    @GET("api/mobile/today")
    suspend fun mobileToday(): MobileTodayResponseDto

    @GET("api/mobile/planned")
    suspend fun mobilePlanned(): MobilePlannedResponseDto

    @GET("api/mobile/challenges")
    suspend fun mobileChallenges(
        @Query("month") month: String,
        @Query("status") status: String
    ): MobileChallengesResponseDto

    @GET("api/mobile/challenges/create-options")
    suspend fun mobileCreateOptions(): MobileCreateOptionsResponseDto

    @POST("api/mobile/challenges/store")
    suspend fun storeChallenge(@Body request: MobileChallengeCreateRequestDto): MobileActionResponseDto

    @POST("api/mobile/challenges/save-details")
    suspend fun saveChallengeDetails(@Body request: MobileChallengeDetailsRequestDto): MobileActionResponseDto

    @POST("api/mobile/challenges/complete")
    suspend fun completeChallenge(@Body request: MobileChallengeActionRequestDto): MobileActionResponseDto

    @POST("api/mobile/challenges/miss")
    suspend fun missChallenge(@Body request: MobileChallengeActionRequestDto): MobileActionResponseDto

    @POST("api/mobile/routines/store")
    suspend fun storeRoutine(@Body request: MobileRoutineCreateRequestDto): MobileActionResponseDto
}
