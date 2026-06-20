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
    val pendingWeek: Int
)
