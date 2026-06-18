package mx.com.karedit.codegymapp.di

import android.content.Context
import mx.com.karedit.codegymapp.core.network.RetrofitFactory
import mx.com.karedit.codegymapp.core.session.SessionManager
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.ChallengesRepository
import mx.com.karedit.codegymapp.data.repository.CreateChallengeRepository
import mx.com.karedit.codegymapp.data.repository.PlannedRepository
import mx.com.karedit.codegymapp.data.repository.TodayRepository
import mx.com.karedit.codegymapp.data.security.EncryptedTokenStorage

class AppContainer(context: Context) {
    private val tokenStorage = EncryptedTokenStorage(context.applicationContext)
    val sessionManager = SessionManager(tokenStorage)
    private val api = RetrofitFactory.createApi(sessionManager)
    val authRepository = AuthRepository(api, sessionManager)
    val todayRepository = TodayRepository(api)
    val plannedRepository = PlannedRepository(api)
    val challengesRepository = ChallengesRepository(api)
    val createChallengeRepository = CreateChallengeRepository(api)
}
