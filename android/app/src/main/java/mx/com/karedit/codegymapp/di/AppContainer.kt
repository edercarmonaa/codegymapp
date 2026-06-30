package mx.com.karedit.codegymapp.di

import android.content.Context
import mx.com.karedit.codegymapp.core.network.RetrofitFactory
import mx.com.karedit.codegymapp.core.notifications.FcmTokenRegistrar
import mx.com.karedit.codegymapp.core.session.SessionManager
import mx.com.karedit.codegymapp.data.local.CodeGymDatabase
import mx.com.karedit.codegymapp.data.local.security.DatabasePassphraseProvider
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tokenStorage = EncryptedTokenStorage(context.applicationContext)
    private val database = CodeGymDatabase.getInstance(
        context.applicationContext,
        DatabasePassphraseProvider(context.applicationContext)
    )
    val sessionManager = SessionManager(tokenStorage)
    private val api = RetrofitFactory.createApi(sessionManager)
    val authRepository = AuthRepository(api, sessionManager)
    private val deviceTokenRepository = DeviceTokenRepository(api)
    val fcmTokenRegistrar = FcmTokenRegistrar(authRepository, deviceTokenRepository, applicationScope)
    val summaryRepository = SummaryRepository(api, database.cachedSummaryDao())
    val notificationsRepository = NotificationsRepository(api, database.cachedNotificationDao())
    val todayRepository = TodayRepository(api, database.cachedChallengeDao())
    val plannedRepository = PlannedRepository(api, database.cachedChallengeDao())
    val challengesRepository = ChallengesRepository(api, database.cachedChallengeDao())
    val challengeDetailsRepository = ChallengeDetailsRepository(api)
    val createChallengeRepository = CreateChallengeRepository(api)
    val createRoutineRepository = CreateRoutineRepository(api)
    val goalsRepository = GoalsRepository(api, database.cachedGoalDao())
    val settingsRepository = SettingsRepository(context.applicationContext, api)
}
