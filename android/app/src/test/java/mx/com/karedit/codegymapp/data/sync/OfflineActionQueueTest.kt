package mx.com.karedit.codegymapp.data.sync

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import mx.com.karedit.codegymapp.data.local.dao.PendingActionDao
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineActionQueueTest {
    private val dao = FakePendingActionDao()
    private val queue = OfflineActionQueue(
        dao,
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    )

    @Test
    fun terminalChallengeActionsCollapseToLastState() = runBlocking {
        queue.enqueueChallengeAction(ActionTypes.CHALLENGE_COMPLETE, 7)
        queue.enqueueChallengeAction(ActionTypes.CHALLENGE_CANCEL, 7)

        assertEquals(1, dao.pending().size)
        assertEquals(ActionTypes.CHALLENGE_CANCEL, dao.pending().single().type)
    }

    @Test
    fun rescheduleKeepsOnlyLatestDate() = runBlocking {
        queue.enqueueReschedule(9, "2026-07-12")
        queue.enqueueReschedule(9, "2026-07-15")

        assertEquals(1, dao.pending().size)
        assertTrue(dao.pending().single().payloadJson.contains("2026-07-15"))
    }

    @Test
    fun notificationDeleteReplacesPendingRead() = runBlocking {
        queue.enqueueNotificationAction(ActionTypes.NOTIFICATION_MARK_READ, 3)
        queue.enqueueNotificationAction(ActionTypes.NOTIFICATION_DELETE, 3)

        assertEquals(1, dao.pending().size)
        assertEquals(ActionTypes.NOTIFICATION_DELETE, dao.pending().single().type)
    }

    @Test
    fun goalUpdateKeepsOnlyLatestValues() = runBlocking {
        queue.enqueueGoalUpdate(4, "streak", "monthly", 10, 0, 0, false)
        queue.enqueueGoalUpdate(4, "streak", "monthly", 20, 0, 0, true)

        assertEquals(1, dao.pending().size)
        assertTrue(dao.pending().single().payloadJson.contains("\"targetValue\":20"))
    }
}

private class FakePendingActionDao : PendingActionDao {
    private val actions = mutableListOf<PendingActionEntity>()
    private var nextId = 1L

    override suspend fun pending(): List<PendingActionEntity> = actions.sortedBy { it.createdAt }

    override fun pendingCountFlow(): Flow<Int> = flowOf(actions.size)

    override suspend fun findByTypeAndPayloadPattern(
        type: String,
        payloadPattern: String
    ): PendingActionEntity? = actions.firstOrNull {
        it.type == type && matchesId(it.payloadJson, payloadPattern)
    }

    override suspend fun insert(action: PendingActionEntity) {
        actions += action.copy(id = nextId++)
    }

    override suspend fun delete(action: PendingActionEntity) {
        actions.removeAll { it.id == action.id }
    }

    override suspend fun markFailed(id: Long, message: String) {
        replace(id) { it.copy(attempts = it.attempts + 1, lastError = message) }
    }

    override suspend fun updatePayload(id: Long, payloadJson: String) {
        replace(id) { it.copy(payloadJson = payloadJson, lastError = "") }
    }

    override suspend fun deleteByTypesAndPayloadPattern(types: List<String>, payloadPattern: String) {
        actions.removeAll { it.type in types && matchesId(it.payloadJson, payloadPattern) }
    }

    private fun replace(id: Long, transform: (PendingActionEntity) -> PendingActionEntity) {
        val index = actions.indexOfFirst { it.id == id }
        if (index >= 0) actions[index] = transform(actions[index])
    }

    private fun matchesId(payload: String, pattern: String): Boolean {
        val id = Regex("-?\\d+").find(pattern)?.value ?: return false
        return payload.contains("\"id\":$id")
    }
}
