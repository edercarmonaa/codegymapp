package mx.com.karedit.codegymapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val payloadJson: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String = ""
)
