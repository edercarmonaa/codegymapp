package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileSummaryDto
import mx.com.karedit.codegymapp.domain.model.MobileSummary

class SummaryRepository(private val api: CodeGymApi) {
    suspend fun summary(): Result<MobileSummary> = runCatching {
        val response = api.mobileSummary()
        if (!response.ok) {
            error(response.message ?: "No se pudo cargar el resumen.")
        }

        response.summary?.toDomain() ?: error("No se pudo cargar el resumen.")
    }
}

private fun MobileSummaryDto.toDomain(): MobileSummary =
    MobileSummary(
        completedMonth = completedMonth,
        generalPercent = generalPercent,
        onTimePercent = onTimePercent,
        timeMonth = timeMonth,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        monthStreak = monthStreak,
        expiredReview = expiredReview,
        pendingToday = pendingToday,
        pendingWeek = pendingWeek
    )
