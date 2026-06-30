package mx.com.karedit.codegymapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_notifications")
data class CachedNotificationEntity(
    @PrimaryKey val id: Int,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val actionUrl: String,
    val createdAt: String,
    val cachedAt: Long
)
