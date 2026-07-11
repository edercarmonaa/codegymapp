package mx.com.karedit.codegymapp.core.session

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mx.com.karedit.codegymapp.data.security.TokenStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionManagerTest {
    @Test
    fun inactivityClearsTokenAndEmitsExpiredReason() = runBlocking {
        val storage = FakeTokenStorage("jwt")
        val manager = SessionManager(storage)
        val event = async(start = CoroutineStart.UNDISPATCHED) { manager.sessionEvents.first() }

        manager.expireIfInactive(Long.MAX_VALUE)

        assertNull(storage.getToken())
        assertEquals(
            SessionExpiredReason.Inactivity,
            (event.await() as SessionEvent.SessionExpired).reason
        )
    }

    @Test
    fun activeSessionIsNotExpiredBeforeTimeout() {
        val storage = FakeTokenStorage("jwt")
        val manager = SessionManager(storage)
        val now = System.currentTimeMillis()

        manager.recordInteraction()
        manager.expireIfInactive(now)

        assertEquals("jwt", storage.getToken())
    }

    @Test
    fun repeatedUnauthorizedResponsesClearSessionOnlyOnce() {
        val storage = FakeTokenStorage("jwt")
        val manager = SessionManager(storage)

        manager.clearSession(SessionExpiredReason.Unauthorized)
        manager.clearSession(SessionExpiredReason.Unauthorized)

        assertEquals(1, storage.clearCount)
    }
}

private class FakeTokenStorage(private var token: String?) : TokenStorage {
    var clearCount: Int = 0
        private set

    override fun getToken(): String? = token

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun clearToken() {
        clearCount++
        token = null
    }
}
