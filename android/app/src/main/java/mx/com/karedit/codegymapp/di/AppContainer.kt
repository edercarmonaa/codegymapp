package mx.com.karedit.codegymapp.di

import android.content.Context
import mx.com.karedit.codegymapp.core.network.RetrofitFactory
import mx.com.karedit.codegymapp.core.notifications.FcmTokenRegistrar
import mx.com.karedit.codegymapp.core.session.SessionManager
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.ChallengeDetailsRepository
import mx.com.karedit.codegymapp.data.repository.ChallengesRepository
import mx.com.karedit.codegymapp.data.repository.CreateChallengeRepository
import mx.com.karedit.codegymapp.data.repository.CreateRoutineRepository
import mx.com.karedit.codegymapp.data.repository.DeviceTokenRepository
import mx.com.karedit.codegymapp.data.repository.NotificationsRepository
import mx.com.karedit.codegymapp.data.repository.PlannedRepository
import mx.com.karedit.codegymapp.data.repository.SummaryRepository
import mx.com.karedit.codegymapp.data.repository.TodayRepository
import mx.com.karedit.codegymapp.data.security.EncryptedTokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tokenStorage = EncryptedTokenStorage(context.applicationContext)
    val sessionManager = SessionManager(tokenStorage)
    private val api = RetrofitFactory.createApi(sessionManager)
    val authRepository = AuthRepository(api, sessionManager)
    private val deviceTokenRepository = DeviceTokenRepository(api)
    val fcmTokenRegistrar = FcmTokenRegistrar(authRepository, deviceTokenRepository, applicationScope)
    val summaryRepository = SummaryRepository(api)
    val notificationsRepository = NotificationsRepository(api)
    val todayRepository = TodayRepository(api)
    val plannedRepository = PlannedRepository(api)
    val challengesRepository = ChallengesRepository(api)
    val challengeDetailsRepository = ChallengeDetailsRepository(api)
    val createChallengeRepository = CreateChallengeRepository(api)
    val createRoutineRepository = CreateRoutineRepository(api)
}
