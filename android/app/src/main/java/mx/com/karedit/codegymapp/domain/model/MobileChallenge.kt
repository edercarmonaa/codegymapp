package mx.com.karedit.codegymapp.domain.model

data class MobileChallenge(
    val id: Int,
    val platformName: String,
    val title: String,
    val scheduledDate: String,
    val completedDate: String?,
    val status: String,
    val difficulty: String,
    val challengeUrl: String?,
    val timeSpentMinutes: Int,
    val notes: String,
    val origin: String,
    val isRescheduled: Boolean
)
