package mx.com.karedit.codegymapp.data.local.mapper

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import mx.com.karedit.codegymapp.data.local.entity.CachedChallengeEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedGoalEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedNotificationEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedSummaryEntity
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.domain.model.MobileGoal
import mx.com.karedit.codegymapp.domain.model.MobileNotification
import mx.com.karedit.codegymapp.domain.model.MobileSummary
import mx.com.karedit.codegymapp.domain.model.MobileSummaryDistribution
import mx.com.karedit.codegymapp.domain.model.MobileSummarySeries

fun MobileChallenge.toCacheEntity(section: String, cachedAt: Long = System.currentTimeMillis()): CachedChallengeEntity =
    CachedChallengeEntity(
        section = section,
        id = id,
        platformId = platformId,
        platformName = platformName,
        title = title,
        scheduledDate = scheduledDate,
        completedDate = completedDate,
        status = status,
        difficulty = difficulty,
        challengeUrl = challengeUrl,
        timeSpentMinutes = timeSpentMinutes,
        notes = notes,
        languageIds = languageIds.joinToString(","),
        languageNames = languageNames,
        githubLinks = githubLinks.joinToString("\n"),
        origin = origin,
        isRescheduled = isRescheduled,
        cachedAt = cachedAt
    )

fun CachedChallengeEntity.toDomain(): MobileChallenge =
    MobileChallenge(
        id = id,
        platformId = platformId,
        platformName = platformName,
        title = title,
        scheduledDate = scheduledDate,
        completedDate = completedDate,
        status = status,
        difficulty = difficulty,
        challengeUrl = challengeUrl,
        timeSpentMinutes = timeSpentMinutes,
        notes = notes,
        languageIds = languageIds.split(",").mapNotNull { it.toIntOrNull() },
        languageNames = languageNames,
        githubLinks = githubLinks.lines().filter { it.isNotBlank() },
        origin = origin,
        isRescheduled = isRescheduled
    )

fun MobileNotification.toCacheEntity(cachedAt: Long = System.currentTimeMillis()): CachedNotificationEntity =
    CachedNotificationEntity(
        id = id,
        type = type,
        title = title,
        message = message,
        isRead = isRead,
        actionUrl = actionUrl,
        createdAt = createdAt,
        cachedAt = cachedAt
    )

fun CachedNotificationEntity.toDomain(): MobileNotification =
    MobileNotification(
        id = id,
        type = type,
        title = title,
        message = message,
        isRead = isRead,
        actionUrl = actionUrl,
        createdAt = createdAt
    )

fun MobileGoal.toCacheEntity(cachedAt: Long = System.currentTimeMillis()): CachedGoalEntity =
    CachedGoalEntity(
        id = id,
        goalType = goalType,
        goalTypeLabel = goalTypeLabel,
        periodType = periodType,
        periodTypeLabel = periodTypeLabel,
        targetValue = targetValue,
        currentValue = currentValue,
        progressPercent = progressPercent,
        platformId = platformId,
        platformName = platformName,
        languageId = languageId,
        languageName = languageName,
        periodStart = periodStart,
        periodEnd = periodEnd,
        autoRenew = autoRenew,
        cachedAt = cachedAt
    )

fun CachedGoalEntity.toDomain(): MobileGoal =
    MobileGoal(
        id = id,
        goalType = goalType,
        goalTypeLabel = goalTypeLabel,
        periodType = periodType,
        periodTypeLabel = periodTypeLabel,
        targetValue = targetValue,
        currentValue = currentValue,
        progressPercent = progressPercent,
        platformId = platformId,
        platformName = platformName,
        languageId = languageId,
        languageName = languageName,
        periodStart = periodStart,
        periodEnd = periodEnd,
        autoRenew = autoRenew
    )

fun MobileSummary.toCacheEntity(month: String, moshi: Moshi, cachedAt: Long = System.currentTimeMillis()): CachedSummaryEntity {
    val seriesJsonAdapter = moshi.adapter<List<MobileSummarySeries>>(
        Types.newParameterizedType(List::class.java, MobileSummarySeries::class.java)
    )

    return CachedSummaryEntity(
        id = month,
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
        distributionPending = distribution.pending,
        distributionCompleted = distribution.completed,
        distributionMissed = distribution.missed,
        distributionExpired = distribution.expired,
        distributionCancelled = distribution.cancelled,
        weeklyComplianceJson = seriesJsonAdapter.toJson(weeklyCompliance),
        topPlatformsJson = seriesJsonAdapter.toJson(topPlatforms),
        topLanguagesJson = seriesJsonAdapter.toJson(topLanguages),
        cachedAt = cachedAt
    )
}

fun CachedSummaryEntity.toDomain(moshi: Moshi): MobileSummary {
    val seriesJsonAdapter = moshi.adapter<List<MobileSummarySeries>>(
        Types.newParameterizedType(List::class.java, MobileSummarySeries::class.java)
    )

    return MobileSummary(
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
        distribution = MobileSummaryDistribution(
            pending = distributionPending,
            completed = distributionCompleted,
            missed = distributionMissed,
            expired = distributionExpired,
            cancelled = distributionCancelled
        ),
        weeklyCompliance = seriesJsonAdapter.fromJson(weeklyComplianceJson).orEmpty(),
        topPlatforms = seriesJsonAdapter.fromJson(topPlatformsJson).orEmpty(),
        topLanguages = seriesJsonAdapter.fromJson(topLanguagesJson).orEmpty()
    )
}
