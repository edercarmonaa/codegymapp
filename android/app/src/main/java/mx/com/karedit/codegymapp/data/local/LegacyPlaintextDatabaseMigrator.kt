package mx.com.karedit.codegymapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mx.com.karedit.codegymapp.data.local.dao.PendingActionDao
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity

class LegacyPlaintextDatabaseMigrator(
    context: Context,
    private val pendingActionDao: PendingActionDao
) {
    private val applicationContext = context.applicationContext

    suspend fun migrate(): Int = migrationMutex.withLock {
        withContext(Dispatchers.IO) {
            val legacyFile = applicationContext.getDatabasePath(LEGACY_DATABASE_NAME)
            if (!legacyFile.exists()) return@withContext 0

            val actions = readPendingActions(legacyFile.path)
            val imported = importLegacyPendingActions(actions, pendingActionDao)

            check(applicationContext.deleteDatabase(LEGACY_DATABASE_NAME)) {
                "No se pudo eliminar la base local heredada sin cifrar."
            }
            imported
        }
    }

    private fun readPendingActions(path: String): List<PendingActionEntity> {
        val database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
        return try {
            database.query(
                "pending_actions",
                arrayOf("type", "payloadJson", "createdAt", "attempts", "lastError"),
                null,
                null,
                null,
                null,
                "createdAt ASC"
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            PendingActionEntity(
                                type = cursor.getString(0),
                                payloadJson = cursor.getString(1),
                                createdAt = cursor.getLong(2),
                                attempts = cursor.getInt(3),
                                lastError = cursor.getString(4).orEmpty()
                            )
                        )
                    }
                }
            }
        } finally {
            database.close()
        }
    }

    companion object {
        const val LEGACY_DATABASE_NAME = "codegym_offline.db"
        private val migrationMutex = Mutex()
    }
}

internal suspend fun importLegacyPendingActions(
    actions: List<PendingActionEntity>,
    pendingActionDao: PendingActionDao
): Int {
    var imported = 0
    actions.forEach { action ->
        if (pendingActionDao.findExact(action.type, action.payloadJson) == null) {
            pendingActionDao.insert(action)
            imported++
        }
    }
    return imported
}
