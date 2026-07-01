package mx.com.karedit.codegymapp.data.local.entity

import androidx.room.Entity

@Entity(tableName = "cached_platforms", primaryKeys = ["id"])
data class CachedPlatformEntity(
    val id: Int,
    val name: String,
    val cachedAt: Long
)

@Entity(tableName = "cached_languages", primaryKeys = ["id"])
data class CachedLanguageEntity(
    val id: Int,
    val name: String,
    val cachedAt: Long
)
