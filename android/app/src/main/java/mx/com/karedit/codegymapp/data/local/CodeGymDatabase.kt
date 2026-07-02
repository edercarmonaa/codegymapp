package mx.com.karedit.codegymapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.com.karedit.codegymapp.data.local.dao.CachedCatalogDao
import mx.com.karedit.codegymapp.data.local.dao.CachedChallengeDao
import mx.com.karedit.codegymapp.data.local.dao.CachedGoalDao
import mx.com.karedit.codegymapp.data.local.dao.CachedNotificationDao
import mx.com.karedit.codegymapp.data.local.dao.CachedSummaryDao
import mx.com.karedit.codegymapp.data.local.dao.PendingActionDao
import mx.com.karedit.codegymapp.data.local.entity.CachedLanguageEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedPlatformEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedCatalogOptionEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedChallengeEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedGoalEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedNotificationEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedSummaryEntity
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity
import mx.com.karedit.codegymapp.data.security.DatabaseKeyProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        CachedChallengeEntity::class,
        CachedNotificationEntity::class,
        CachedSummaryEntity::class,
        CachedGoalEntity::class,
        PendingActionEntity::class,
        CachedPlatformEntity::class,
        CachedLanguageEntity::class,
        CachedCatalogOptionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class CodeGymDatabase : RoomDatabase() {
    abstract fun cachedChallengeDao(): CachedChallengeDao
    abstract fun cachedNotificationDao(): CachedNotificationDao
    abstract fun cachedSummaryDao(): CachedSummaryDao
    abstract fun cachedGoalDao(): CachedGoalDao
    abstract fun cachedCatalogDao(): CachedCatalogDao
    abstract fun pendingActionDao(): PendingActionDao

    companion object {
        @Volatile
        private var instance: CodeGymDatabase? = null

        fun getInstance(context: Context): CodeGymDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CodeGymDatabase::class.java,
                    "codegym_offline_encrypted.db"
                )
                    .openHelperFactory(
                        SupportOpenHelperFactory(DatabaseKeyProvider(context.applicationContext).passphrase())
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
