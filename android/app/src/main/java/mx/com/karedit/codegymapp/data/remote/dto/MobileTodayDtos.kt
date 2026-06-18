package mx.com.karedit.codegymapp.data.remote.dto

import com.squareup.moshi.Json

data class MobileTodayResponseDto(
    val ok: Boolean,
    val today: List<MobileChallengeDto> = emptyList(),
    val expired: List<MobileChallengeDto> = emptyList(),
    val message: String? = null
)

data class MobilePlannedResponseDto(
    val ok: Boolean,
    val planned: List<MobileChallengeDto> = emptyList(),
    val message: String? = null
)

data class MobileChallengesResponseDto(
    val ok: Boolean,
    val filters: MobileChallengeFiltersDto? = null,
    val challenges: List<MobileChallengeDto> = emptyList(),
    val message: String? = null
)

data class MobileChallengeFiltersDto(
    val month: String,
    val status: String
)

data class MobileChallengeActionRequestDto(
    val id: Int
)

data class MobileChallengeCreateRequestDto(
    @Json(name = "platform_id") val platformId: Int,
    @Json(name = "scheduled_date") val scheduledDate: String
)

data class MobileRoutineCreateRequestDto(
    @Json(name = "platform_id") val platformId: Int,
    @Json(name = "frequency_type") val frequencyType: String,
    @Json(name = "week_days") val weekDays: List<Int> = emptyList(),
    @Json(name = "month_day") val monthDay: Int = 0,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String = ""
)

data class MobileCreateOptionsResponseDto(
    val ok: Boolean,
    val platforms: List<MobilePlatformDto> = emptyList(),
    val message: String? = null
)

data class MobilePlatformDto(
    val id: Int,
    val name: String
)

data class MobileActionResponseDto(
    val ok: Boolean,
    val message: String? = null
)

data class MobileChallengeDto(
    val id: Int,
    @Json(name = "platform_name") val platformName: String,
    val title: String? = null,
    @Json(name = "scheduled_date") val scheduledDate: String,
    @Json(name = "completed_date") val completedDate: String? = null,
    val status: String,
    val difficulty: String? = null,
    @Json(name = "challenge_url") val challengeUrl: String? = null,
    @Json(name = "time_spent_minutes") val timeSpentMinutes: Int = 0,
    val notes: String? = null,
    val origin: String? = null,
    @Json(name = "is_rescheduled") val isRescheduled: Boolean = false
)
