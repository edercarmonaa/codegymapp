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

data class MobileSummaryResponseDto(
    val ok: Boolean,
    val summary: MobileSummaryDto? = null,
    val message: String? = null
)

data class MobileSummaryDto(
    @Json(name = "completed_month") val completedMonth: Int = 0,
    @Json(name = "general_percent") val generalPercent: Double = 0.0,
    @Json(name = "on_time_percent") val onTimePercent: Double = 0.0,
    @Json(name = "time_month") val timeMonth: Int = 0,
    @Json(name = "current_streak") val currentStreak: Int = 0,
    @Json(name = "best_streak") val bestStreak: Int = 0,
    @Json(name = "month_streak") val monthStreak: Int = 0,
    @Json(name = "expired_review") val expiredReview: Int = 0,
    @Json(name = "pending_today") val pendingToday: Int = 0,
    @Json(name = "pending_week") val pendingWeek: Int = 0,
    @Json(name = "days_without_practice") val daysWithoutPractice: Int = 0,
    val distribution: MobileSummaryDistributionDto = MobileSummaryDistributionDto(),
    @Json(name = "weekly_compliance") val weeklyCompliance: List<MobileSummarySeriesDto> = emptyList(),
    @Json(name = "top_platforms") val topPlatforms: List<MobileSummarySeriesDto> = emptyList(),
    @Json(name = "top_languages") val topLanguages: List<MobileSummarySeriesDto> = emptyList()
)

data class MobileSummaryDistributionDto(
    val pending: Int = 0,
    val completed: Int = 0,
    val missed: Int = 0,
    val expired: Int = 0,
    val cancelled: Int = 0
)

data class MobileSummarySeriesDto(
    val label: String = "",
    val value: Int = 0,
    val minutes: Int = 0,
    val scheduled: Int = 0,
    val completed: Int = 0,
    val percent: Double = 0.0
)

data class MobileNotificationsResponseDto(
    val ok: Boolean,
    @Json(name = "unread_count") val unreadCount: Int = 0,
    val notifications: List<MobileNotificationDto> = emptyList(),
    val message: String? = null
)

data class MobileNotificationDto(
    val id: Int,
    val type: String = "",
    val title: String,
    val message: String,
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "action_url") val actionUrl: String = "",
    @Json(name = "created_at") val createdAt: String = ""
)

data class MobileNotificationActionRequestDto(
    val id: Int
)

data class MobileDeviceTokenRequestDto(
    val token: String,
    val platform: String = "android",
    @Json(name = "device_name") val deviceName: String,
    @Json(name = "app_version") val appVersion: String,
    @Json(name = "push_enabled") val pushEnabled: Boolean = true,
    @Json(name = "reminder_time") val reminderTime: String = "08:00"
)

data class MobileChallengeFiltersDto(
    val month: String,
    val status: String
)

data class MobileChallengeActionRequestDto(
    val id: Int
)

data class MobileChallengeRescheduleRequestDto(
    val id: Int,
    @Json(name = "scheduled_date") val scheduledDate: String
)

data class MobileChallengeDetailsRequestDto(
    val id: Int,
    @Json(name = "platform_id") val platformId: Int,
    val title: String,
    @Json(name = "challenge_url") val challengeUrl: String,
    val difficulty: String,
    @Json(name = "time_spent_minutes") val timeSpentMinutes: Int?,
    val notes: String,
    @Json(name = "language_ids") val languageIds: List<Int>,
    @Json(name = "github_links") val githubLinks: String
)

data class MobileChallengeCreateRequestDto(
    @Json(name = "platform_id") val platformId: Int,
    @Json(name = "scheduled_date") val scheduledDate: String
)

data class MobileManualChallengeRequestDto(
    @Json(name = "platform_id") val platformId: Int,
    val title: String,
    @Json(name = "challenge_url") val challengeUrl: String = "",
    val difficulty: String,
    @Json(name = "time_spent_minutes") val timeSpentMinutes: Int,
    val notes: String = "",
    @Json(name = "language_ids") val languageIds: List<Int>,
    @Json(name = "github_links") val githubLinks: String = ""
)

data class MobileRoutineCreateRequestDto(
    @Json(name = "platform_id") val platformId: Int,
    @Json(name = "frequency_type") val frequencyType: String,
    @Json(name = "week_days") val weekDays: List<Int> = emptyList(),
    @Json(name = "month_day") val monthDay: Int = 0,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String = ""
)

data class MobileGoalOptionsResponseDto(
    val ok: Boolean,
    @Json(name = "goal_types") val goalTypes: Map<String, String> = emptyMap(),
    @Json(name = "period_types") val periodTypes: Map<String, String> = emptyMap(),
    val platforms: List<MobilePlatformDto> = emptyList(),
    val languages: List<MobileLanguageDto> = emptyList(),
    val message: String? = null
)

data class MobileGoalsResponseDto(
    val ok: Boolean,
    @Json(name = "goal_types") val goalTypes: Map<String, String> = emptyMap(),
    @Json(name = "period_types") val periodTypes: Map<String, String> = emptyMap(),
    val goals: List<MobileGoalDto> = emptyList(),
    val message: String? = null
)

data class MobileGoalDto(
    val id: Int,
    @Json(name = "goal_type") val goalType: String,
    @Json(name = "period_type") val periodType: String,
    @Json(name = "target_value") val targetValue: Int,
    @Json(name = "current_value") val currentValue: Int,
    @Json(name = "progress_percent") val progressPercent: Double,
    @Json(name = "platform_id") val platformId: Int = 0,
    @Json(name = "platform_name") val platformName: String = "",
    @Json(name = "language_id") val languageId: Int = 0,
    @Json(name = "language_name") val languageName: String = "",
    @Json(name = "period_start") val periodStart: String = "",
    @Json(name = "period_end") val periodEnd: String = "",
    @Json(name = "auto_renew") val autoRenew: Boolean = false
)

data class MobileGoalCreateRequestDto(
    @Json(name = "goal_type") val goalType: String,
    @Json(name = "period_type") val periodType: String,
    @Json(name = "target_value") val targetValue: Int,
    @Json(name = "platform_id") val platformId: Int = 0,
    @Json(name = "language_id") val languageId: Int = 0,
    @Json(name = "auto_renew") val autoRenew: Boolean = false
)

data class MobileGoalUpdateRequestDto(
    val id: Int,
    @Json(name = "goal_type") val goalType: String,
    @Json(name = "period_type") val periodType: String,
    @Json(name = "target_value") val targetValue: Int,
    @Json(name = "platform_id") val platformId: Int = 0,
    @Json(name = "language_id") val languageId: Int = 0,
    @Json(name = "auto_renew") val autoRenew: Boolean = false
)

data class MobileThemeRequestDto(
    val theme: String
)

data class MobileSettingsRequestDto(
    val theme: String? = null,
    @Json(name = "push_enabled") val pushEnabled: Boolean,
    @Json(name = "reminder_time") val reminderTime: String
)

data class MobileCreateOptionsResponseDto(
    val ok: Boolean,
    val platforms: List<MobilePlatformDto> = emptyList(),
    val languages: List<MobileLanguageDto> = emptyList(),
    val message: String? = null
)

data class MobilePlatformDto(
    val id: Int,
    val name: String
)

data class MobileLanguageDto(
    val id: Int,
    val name: String
)

data class MobileActionResponseDto(
    val ok: Boolean,
    val id: Int? = null,
    val message: String? = null
)

data class MobileChallengeDto(
    val id: Int,
    @Json(name = "platform_id") val platformId: Int = 0,
    @Json(name = "platform_name") val platformName: String,
    val title: String? = null,
    @Json(name = "scheduled_date") val scheduledDate: String,
    @Json(name = "completed_date") val completedDate: String? = null,
    val status: String,
    val difficulty: String? = null,
    @Json(name = "challenge_url") val challengeUrl: String? = null,
    @Json(name = "time_spent_minutes") val timeSpentMinutes: Int = 0,
    val notes: String? = null,
    @Json(name = "language_ids") val languageIds: List<Int> = emptyList(),
    @Json(name = "language_names") val languageNames: String = "",
    @Json(name = "github_links") val githubLinks: List<MobileGithubLinkDto> = emptyList(),
    val origin: String? = null,
    @Json(name = "is_rescheduled") val isRescheduled: Boolean = false
)

data class MobileGithubLinkDto(
    @Json(name = "github_url") val githubUrl: String,
    val description: String = ""
)
