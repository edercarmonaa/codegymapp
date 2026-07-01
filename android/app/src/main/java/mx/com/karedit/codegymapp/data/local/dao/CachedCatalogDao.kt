package mx.com.karedit.codegymapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import mx.com.karedit.codegymapp.data.local.entity.CachedLanguageEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedPlatformEntity

@Dao
interface CachedCatalogDao {
    @Query("SELECT * FROM cached_platforms ORDER BY name")
    suspend fun platforms(): List<CachedPlatformEntity>

    @Query("SELECT * FROM cached_languages ORDER BY name")
    suspend fun languages(): List<CachedLanguageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatforms(platforms: List<CachedPlatformEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languages: List<CachedLanguageEntity>)

    @Query("DELETE FROM cached_platforms")
    suspend fun clearPlatforms()

    @Query("DELETE FROM cached_languages")
    suspend fun clearLanguages()

    @Transaction
    suspend fun replacePlatforms(platforms: List<CachedPlatformEntity>) {
        clearPlatforms()
        insertPlatforms(platforms)
    }

    @Transaction
    suspend fun replaceLanguages(languages: List<CachedLanguageEntity>) {
        clearLanguages()
        insertLanguages(languages)
    }
}
