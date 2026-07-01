package mx.com.karedit.codegymapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import mx.com.karedit.codegymapp.data.local.entity.CachedGoalEntity

@Dao
interface CachedGoalDao {
    @Query("SELECT * FROM cached_goals ORDER BY periodEnd ASC, id ASC")
    suspend fun all(): List<CachedGoalEntity>

    @Query("DELETE FROM cached_goals")
    suspend fun deleteAll()

    @Query("""
        UPDATE cached_goals
        SET goalType = :goalType,
            periodType = :periodType,
            targetValue = :targetValue,
            platformId = :platformId,
            languageId = :languageId,
            autoRenew = :autoRenew
        WHERE id = :id
    """)
    suspend fun updateGoal(
        id: Int,
        goalType: String,
        periodType: String,
        targetValue: Int,
        platformId: Int,
        languageId: Int,
        autoRenew: Boolean
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<CachedGoalEntity>)

    @Transaction
    suspend fun replaceAll(goals: List<CachedGoalEntity>) {
        deleteAll()
        insertAll(goals)
    }
}
