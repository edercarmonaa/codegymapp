package mx.com.karedit.codegymapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import mx.com.karedit.codegymapp.data.local.entity.CachedChallengeEntity

@Dao
interface CachedChallengeDao {
    @Query("SELECT * FROM cached_challenges WHERE section = :section ORDER BY scheduledDate ASC, id ASC")
    suspend fun getBySection(section: String): List<CachedChallengeEntity>

    @Query("DELETE FROM cached_challenges WHERE section = :section")
    suspend fun deleteSection(section: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<CachedChallengeEntity>)

    @Transaction
    suspend fun replaceSection(section: String, challenges: List<CachedChallengeEntity>) {
        deleteSection(section)
        insertAll(challenges)
    }
}
