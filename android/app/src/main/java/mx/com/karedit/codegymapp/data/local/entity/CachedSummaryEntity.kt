package mx.com.karedit.codegymapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_summary")
data class CachedSummaryEntity(
    @PrimaryKey val id: String,
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
    val distributionPending: Int,
    val distributionCompleted: Int,
    val distributionMissed: Int,
    val distributionExpired: Int,
    val distributionCancelled: Int,
    val weeklyComplianceJson: String,
    val topPlatformsJson: String,
    val topLanguagesJson: String,
    val cachedAt: Long
)
