package mx.com.karedit.codegymapp.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.YearMonth
import mx.com.karedit.codegymapp.core.network.NetworkMonitor
import mx.com.karedit.codegymapp.core.network.RetrofitFactory
import mx.com.karedit.codegymapp.core.notifications.FcmTokenRegistrar
import mx.com.karedit.codegymapp.core.session.SessionManager
import mx.com.karedit.codegymapp.data.local.CodeGymDatabase
import mx.com.karedit.codegymapp.data.local.LegacyPlaintextDatabaseMigrator
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.ChallengeDetailsRepository
import mx.com.karedit.codegymapp.data.repository.ChallengesRepository
import mx.com.karedit.codegymapp.data.repository.CreateChallengeRepository
import mx.com.karedit.codegymapp.data.repository.CreateRoutineRepository
import mx.com.karedit.codegymapp.data.repository.DeviceTokenRepository
import mx.com.karedit.codegymapp.data.repository.GoalsRepository
import mx.com.karedit.codegymapp.data.repository.NotificationsRepository
import mx.com.karedit.codegymapp.data.repository.PlannedRepository
import mx.com.karedit.codegymapp.data.repository.SettingsRepository
import mx.com.karedit.codegymapp.data.repository.SummaryRepository
import mx.com.karedit.codegymapp.data.repository.TodayRepository
import mx.com.karedit.codegymapp.data.security.EncryptedTokenStorage
import mx.com.karedit.codegymapp.data.sync.OfflineActionQueue
import mx.com.karedit.codegymapp.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val networkMonitor = NetworkMonitor(context.applicationContext)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val tokenStorage = EncryptedTokenStorage(context.applicationContext)
    private val database = CodeGymDatabase.getInstance(context.applicationContext)
    private val legacyDatabaseMigration = applicationScope.async {
        LegacyPlaintextDatabaseMigrator(
            context.applicationContext,
            database.pendingActionDao()
        ).migrate()
    }
    val offlineActionQueue = OfflineActionQueue(database.pendingActionDao(), moshi)
    val sessionManager = SessionManager(tokenStorage)
    private val api = RetrofitFactory.createApi(sessionManager)
    val syncManager = SyncManager(api, database.pendingActionDao(), database.cachedChallengeDao(), moshi)
    val authRepository = AuthRepository(api, sessionManager)
    private val deviceTokenRepository = DeviceTokenRepository(api)
    val settingsRepository = SettingsRepository(context.applicationContext, api)
    val fcmTokenRegistrar = FcmTokenRegistrar(authRepository, deviceTokenRepository, settingsRepository, applicationScope)
    val summaryRepository = SummaryRepository(api, database.cachedSummaryDao())
    val notificationsRepository = NotificationsRepository(api, database.cachedNotificationDao(), offlineActionQueue)
    val todayRepository = TodayRepository(api, database.cachedChallengeDao(), offlineActionQueue)
    val plannedRepository = PlannedRepository(api, database.cachedChallengeDao(), offlineActionQueue)
    val challengesRepository = ChallengesRepository(api, database.cachedChallengeDao(), offlineActionQueue)
    val challengeDetailsRepository = ChallengeDetailsRepository(
        api,
        database.cachedCatalogDao(),
        database.cachedChallengeDao(),
        offlineActionQueue
    )
    val createChallengeRepository = CreateChallengeRepository(
        api,
        database.cachedCatalogDao(),
        database.cachedChallengeDao(),
        offlineActionQueue
    )
    val createRoutineRepository = CreateRoutineRepository(api, database.cachedCatalogDao(), offlineActionQueue)
    val goalsRepository = GoalsRepository(api, database.cachedGoalDao(), database.cachedCatalogDao(), offlineActionQueue)

    init {
        applicationScope.launch {
            legacyDatabaseMigration.await()
            goalsRepository.seedStaticCatalogs()
        }
        applicationScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                if (isOnline && authRepository.hasToken()) {
                    legacyDatabaseMigration.await()
                    syncLocalData()
                }
            }
        }
    }

    fun syncNow() {
        if (!authRepository.hasToken()) {
            return
        }

        applicationScope.launch {
            legacyDatabaseMigration.await()
            syncLocalData()
        }
    }

    private suspend fun syncLocalData() {
        runCatching { syncManager.syncNow() }
        runCatching { goalsRepository.seedStaticCatalogs() }
        runCatching { createChallengeRepository.options() }
        runCatching { goalsRepository.options() }
        runCatching { todayRepository.today() }
        runCatching { plannedRepository.planned() }
        runCatching { challengesRepository.challenges(YearMonth.now().toString(), "all") }
        runCatching { settingsRepository.syncSettings() }
    }
}
