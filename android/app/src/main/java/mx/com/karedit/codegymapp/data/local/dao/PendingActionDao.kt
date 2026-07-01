package mx.com.karedit.codegymapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingActionDao {
    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun pending(): List<PendingActionEntity>

    @Query("SELECT COUNT(*) FROM pending_actions")
    fun pendingCountFlow(): Flow<Int>

    @Insert
    suspend fun insert(action: PendingActionEntity)

    @Delete
    suspend fun delete(action: PendingActionEntity)

    @Query("UPDATE pending_actions SET attempts = attempts + 1, lastError = :message WHERE id = :id")
    suspend fun markFailed(id: Long, message: String)
}
