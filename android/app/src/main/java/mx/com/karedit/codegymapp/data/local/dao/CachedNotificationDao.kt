package mx.com.karedit.codegymapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import mx.com.karedit.codegymapp.data.local.entity.CachedNotificationEntity

@Dao
interface CachedNotificationDao {
    @Query("SELECT * FROM cached_notifications ORDER BY isRead ASC, createdAt DESC, id DESC")
    suspend fun all(): List<CachedNotificationEntity>

    @Query("SELECT COUNT(*) FROM cached_notifications WHERE isRead = 0")
    suspend fun unreadCount(): Int

    @Query("DELETE FROM cached_notifications")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<CachedNotificationEntity>)

    @Transaction
    suspend fun replaceAll(notifications: List<CachedNotificationEntity>) {
        deleteAll()
        insertAll(notifications)
    }
}
