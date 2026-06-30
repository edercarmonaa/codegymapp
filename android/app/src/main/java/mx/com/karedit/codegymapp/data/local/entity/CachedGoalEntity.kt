package mx.com.karedit.codegymapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_goals")
data class CachedGoalEntity(
    @PrimaryKey val id: Int,
    val goalType: String,
    val goalTypeLabel: String,
    val periodType: String,
    val periodTypeLabel: String,
    val targetValue: Int,
    val currentValue: Int,
    val progressPercent: Double,
    val platformId: Int,
    val platformName: String,
    val languageId: Int,
    val languageName: String,
    val periodStart: String,
    val periodEnd: String,
    val autoRenew: Boolean,
    val cachedAt: Long
)
