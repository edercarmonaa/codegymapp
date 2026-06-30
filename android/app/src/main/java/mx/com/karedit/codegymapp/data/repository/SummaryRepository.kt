package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.local.dao.CachedSummaryDao
import mx.com.karedit.codegymapp.data.local.mapper.toCacheEntity
import mx.com.karedit.codegymapp.data.local.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileSummaryDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileSummaryDistributionDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileSummarySeriesDto
import mx.com.karedit.codegymapp.domain.model.MobileSummary
import mx.com.karedit.codegymapp.domain.model.MobileSummaryDistribution
import mx.com.karedit.codegymapp.domain.model.MobileSummarySeries

class SummaryRepository(
    private val api: CodeGymApi,
    private val summaryDao: CachedSummaryDao
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun summary(): Result<MobileSummary> = runCatching {
        try {
            val response = api.mobileSummary()
            if (!response.ok) {
                error(response.message ?: "No se pudo cargar el resumen.")
            }

            val summary = response.summary?.toDomain() ?: error("No se pudo cargar el resumen.")
            summaryDao.upsert(summary.toCacheEntity(moshi))
            summary
        } catch (exception: Exception) {
            summaryDao.get()?.toDomain(moshi) ?: throw exception
        }
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
        pendingWeek = pendingWeek,
        daysWithoutPractice = daysWithoutPractice,
        distribution = distribution.toDomain(),
        weeklyCompliance = weeklyCompliance.map { it.toDomain() },
        topPlatforms = topPlatforms.map { it.toDomain() },
        topLanguages = topLanguages.map { it.toDomain() }
    )

private fun MobileSummaryDistributionDto.toDomain(): MobileSummaryDistribution =
    MobileSummaryDistribution(
        pending = pending,
        completed = completed,
        missed = missed,
        expired = expired,
        cancelled = cancelled
    )

private fun MobileSummarySeriesDto.toDomain(): MobileSummarySeries =
    MobileSummarySeries(
        label = label,
        value = value,
        minutes = minutes,
        scheduled = scheduled,
        completed = completed,
        percent = percent
    )
