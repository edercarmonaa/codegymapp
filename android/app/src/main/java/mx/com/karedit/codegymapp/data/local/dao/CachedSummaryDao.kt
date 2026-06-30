package mx.com.karedit.codegymapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.com.karedit.codegymapp.data.local.entity.CachedSummaryEntity

@Dao
interface CachedSummaryDao {
    @Query("SELECT * FROM cached_summary WHERE id = 'summary' LIMIT 1")
    suspend fun get(): CachedSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: CachedSummaryEntity)
}
