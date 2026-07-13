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

    @Query("SELECT * FROM pending_actions WHERE type = :type AND payloadJson LIKE :payloadPattern ORDER BY createdAt ASC LIMIT 1")
    suspend fun findByTypeAndPayloadPattern(type: String, payloadPattern: String): PendingActionEntity?

    @Query("SELECT * FROM pending_actions WHERE type = :type AND payloadJson = :payloadJson LIMIT 1")
    suspend fun findExact(type: String, payloadJson: String): PendingActionEntity?

    @Insert
    suspend fun insert(action: PendingActionEntity)

    @Delete
    suspend fun delete(action: PendingActionEntity)

    @Query(
        "UPDATE pending_actions SET attempts = attempts + 1, lastError = :message, " +
            "errorKind = :errorKind, lastAttemptAt = :attemptedAt, nextAttemptAt = :nextAttemptAt " +
            "WHERE id = :id"
    )
    suspend fun markFailed(
        id: Long,
        message: String,
        errorKind: String,
        attemptedAt: Long,
        nextAttemptAt: Long
    )

    @Query(
        "UPDATE pending_actions SET payloadJson = :payloadJson, attempts = 0, lastError = '', " +
            "errorKind = '', lastAttemptAt = 0, nextAttemptAt = 0 WHERE id = :id"
    )
    suspend fun updatePayload(id: Long, payloadJson: String)

    @Query("DELETE FROM pending_actions WHERE type IN (:types) AND payloadJson LIKE :payloadPattern")
    suspend fun deleteByTypesAndPayloadPattern(types: List<String>, payloadPattern: String)
}
