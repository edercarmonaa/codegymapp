package mx.com.karedit.codegymapp.data.local.entity

import androidx.room.Entity

@Entity(tableName = "cached_challenges", primaryKeys = ["section", "id"])
data class CachedChallengeEntity(
    val section: String,
    val id: Int,
    val platformId: Int,
    val platformName: String,
    val title: String,
    val scheduledDate: String,
    val completedDate: String?,
    val status: String,
    val difficulty: String,
    val challengeUrl: String?,
    val timeSpentMinutes: Int,
    val notes: String,
    val languageIds: String,
    val languageNames: String,
    val githubLinks: String,
    val origin: String,
    val isRescheduled: Boolean,
    val cachedAt: Long
)
