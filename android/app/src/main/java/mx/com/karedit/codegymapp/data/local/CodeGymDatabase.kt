package mx.com.karedit.codegymapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.com.karedit.codegymapp.data.local.dao.CachedChallengeDao
import mx.com.karedit.codegymapp.data.local.dao.CachedGoalDao
import mx.com.karedit.codegymapp.data.local.dao.CachedNotificationDao
import mx.com.karedit.codegymapp.data.local.dao.CachedSummaryDao
import mx.com.karedit.codegymapp.data.local.entity.CachedChallengeEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedGoalEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedNotificationEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedSummaryEntity
import mx.com.karedit.codegymapp.data.local.security.DatabasePassphraseProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        CachedChallengeEntity::class,
        CachedNotificationEntity::class,
        CachedSummaryEntity::class,
        CachedGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CodeGymDatabase : RoomDatabase() {
    abstract fun cachedChallengeDao(): CachedChallengeDao
    abstract fun cachedNotificationDao(): CachedNotificationDao
    abstract fun cachedSummaryDao(): CachedSummaryDao
    abstract fun cachedGoalDao(): CachedGoalDao

    companion object {
        @Volatile
        private var instance: CodeGymDatabase? = null

        fun getInstance(context: Context, passphraseProvider: DatabasePassphraseProvider): CodeGymDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CodeGymDatabase::class.java,
                    "codegym_offline.db"
                )
                    .openHelperFactory(SupportOpenHelperFactory(passphraseProvider.getOrCreatePassphrase()))
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
