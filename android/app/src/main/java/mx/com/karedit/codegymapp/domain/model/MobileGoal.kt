package mx.com.karedit.codegymapp.domain.model

data class MobileGoal(
    val id: Int,
    val goalType: String,
    val goalTypeLabel: String,
    val periodType: String,
    val periodTypeLabel: String,
    val targetValue: Int,
    val currentValue: Int,
    val progressPercent: Double,
    val platformName: String,
    val languageName: String,
    val periodStart: String,
    val periodEnd: String,
    val autoRenew: Boolean
) {
    val unit: String
        get() = when (goalType) {
            "practice_time" -> "min"
            "streak" -> "días"
            else -> "retos"
        }

    val scopeLabel: String
        get() = listOf(platformName, languageName)
            .filter { it.isNotBlank() }
            .ifEmpty { listOf("General") }
            .joinToString(" · ")
}
