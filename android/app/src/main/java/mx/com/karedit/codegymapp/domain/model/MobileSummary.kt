package mx.com.karedit.codegymapp.domain.model

data class MobileSummary(
    val completedMonth: Int,
    val generalPercent: Double,
    val onTimePercent: Double,
    val timeMonth: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val monthStreak: Int,
    val expiredReview: Int,
    val pendingToday: Int,
    val pendingWeek: Int,
    val daysWithoutPractice: Int,
    val distribution: MobileSummaryDistribution,
    val weeklyCompliance: List<MobileSummarySeries>,
    val topPlatforms: List<MobileSummarySeries>,
    val topLanguages: List<MobileSummarySeries>
)

data class MobileSummaryDistribution(
    val pending: Int,
    val completed: Int,
    val missed: Int,
    val expired: Int,
    val cancelled: Int
)

data class MobileSummarySeries(
    val label: String,
    val value: Int,
    val minutes: Int,
    val scheduled: Int,
    val completed: Int,
    val percent: Double
)
