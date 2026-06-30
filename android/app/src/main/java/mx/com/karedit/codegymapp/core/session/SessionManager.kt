package mx.com.karedit.codegymapp.core.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import mx.com.karedit.codegymapp.data.security.TokenStorage

class SessionManager(private val tokenStorage: TokenStorage) {
    private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents
    private var lastInteractionAt: Long = System.currentTimeMillis()

    fun token(): String? = tokenStorage.getToken()

    fun saveToken(token: String) {
        tokenStorage.saveToken(token)
        recordInteraction()
    }

    fun clearSession(reason: SessionExpiredReason = SessionExpiredReason.Manual) {
        tokenStorage.clearToken()
        _sessionEvents.tryEmit(SessionEvent.SessionExpired(reason))
    }

    fun recordInteraction() {
        lastInteractionAt = System.currentTimeMillis()
    }

    fun expireIfInactive(now: Long = System.currentTimeMillis()) {
        if (token().isNullOrBlank()) {
            return
        }

        if (now - lastInteractionAt >= INACTIVITY_TIMEOUT_MS) {
            clearSession(SessionExpiredReason.Inactivity)
        }
    }

    private companion object {
        const val INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000L
    }
}

sealed interface SessionEvent {
    data class SessionExpired(val reason: SessionExpiredReason) : SessionEvent
}

enum class SessionExpiredReason {
    Unauthorized,
    Inactivity,
    Manual
}
