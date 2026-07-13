package mx.com.karedit.codegymapp.data.local

import kotlinx.coroutines.runBlocking
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity
import mx.com.karedit.codegymapp.data.sync.FakePendingActionDao
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyPlaintextDatabaseMigratorTest {
    @Test
    fun `importa accion heredada una sola vez`() = runBlocking {
        val dao = FakePendingActionDao()
        val action = PendingActionEntity(
            type = "challenge.complete",
            payloadJson = "{\"id\":7}",
            createdAt = 100,
            attempts = 2,
            lastError = "offline"
        )

        assertEquals(1, importLegacyPendingActions(listOf(action), dao))
        assertEquals(0, importLegacyPendingActions(listOf(action), dao))
        assertEquals(1, dao.pending().size)
        assertEquals(2, dao.pending().single().attempts)
    }
}
