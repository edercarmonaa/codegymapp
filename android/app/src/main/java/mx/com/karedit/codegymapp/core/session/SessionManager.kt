package mx.com.karedit.codegymapp.core.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import mx.com.karedit.codegymapp.data.security.TokenStorage

class SessionManager(private val tokenStorage: TokenStorage) {
    private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents

    fun token(): String? = tokenStorage.getToken()

    fun refreshToken(): String? = tokenStorage.getRefreshToken()

    fun saveToken(token: String) {
        tokenStorage.saveToken(token)
    }

    fun saveRefreshToken(token: String) {
        tokenStorage.saveRefreshToken(token)
    }

    fun clearSession(reason: SessionExpiredReason = SessionExpiredReason.Manual) {
        tokenStorage.clearToken()
        tokenStorage.clearRefreshToken()
        _sessionEvents.tryEmit(SessionEvent.SessionExpired(reason))
    }

    fun expireAccessToken(reason: SessionExpiredReason = SessionExpiredReason.Unauthorized) {
        tokenStorage.clearToken()
        _sessionEvents.tryEmit(SessionEvent.SessionExpired(reason))
    }

    fun lockSession() {
        tokenStorage.clearToken()
        _sessionEvents.tryEmit(SessionEvent.SessionLocked)
    }
}

sealed interface SessionEvent {
    data class SessionExpired(val reason: SessionExpiredReason) : SessionEvent
    data object SessionLocked : SessionEvent
}

enum class SessionExpiredReason {
    Unauthorized,
    Inactivity,
    Manual
}
