package mx.com.karedit.codegymapp.data.sync

import com.squareup.moshi.Moshi
import mx.com.karedit.codegymapp.data.local.dao.PendingActionDao
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity

class OfflineActionQueue(
    private val pendingActionDao: PendingActionDao,
    private val moshi: Moshi
) {
    val pendingCount = pendingActionDao.pendingCountFlow()

    suspend fun enqueueChallengeAction(type: String, id: Int) {
        enqueue(type, IdPayload(id))
    }

    suspend fun enqueueReschedule(id: Int, scheduledDate: String) {
        enqueue(ActionTypes.CHALLENGE_RESCHEDULE, ReschedulePayload(id, scheduledDate))
    }

    suspend fun enqueueNotificationAction(type: String, id: Int) {
        enqueue(type, IdPayload(id))
    }

    suspend fun enqueueGoalCreate(
        goalType: String,
        periodType: String,
        targetValue: Int,
        platformId: Int,
        languageId: Int,
        autoRenew: Boolean
    ) {
        enqueue(
            ActionTypes.GOAL_CREATE,
            GoalPayload(
                id = 0,
                goalType = goalType,
                periodType = periodType,
                targetValue = targetValue,
                platformId = platformId,
                languageId = languageId,
                autoRenew = autoRenew
            )
        )
    }

    suspend fun enqueueGoalUpdate(
        id: Int,
        goalType: String,
        periodType: String,
        targetValue: Int,
        platformId: Int,
        languageId: Int,
        autoRenew: Boolean
    ) {
        enqueue(
            ActionTypes.GOAL_UPDATE,
            GoalPayload(id, goalType, periodType, targetValue, platformId, languageId, autoRenew)
        )
    }

    private inline fun <reified T> adapter() = moshi.adapter(T::class.java)

    private suspend inline fun <reified T> enqueue(type: String, payload: T) {
        pendingActionDao.insert(
            PendingActionEntity(
                type = type,
                payloadJson = adapter<T>().toJson(payload),
                createdAt = System.currentTimeMillis()
            )
        )
    }
}

object ActionTypes {
    const val CHALLENGE_COMPLETE = "challenge.complete"
    const val CHALLENGE_MISS = "challenge.miss"
    const val CHALLENGE_CANCEL = "challenge.cancel"
    const val CHALLENGE_RESCHEDULE = "challenge.reschedule"
    const val NOTIFICATION_MARK_READ = "notification.mark_read"
    const val NOTIFICATION_DELETE = "notification.delete"
    const val GOAL_CREATE = "goal.create"
    const val GOAL_UPDATE = "goal.update"
}

data class IdPayload(val id: Int)
data class ReschedulePayload(val id: Int, val scheduledDate: String)
data class GoalPayload(
    val id: Int,
    val goalType: String,
    val periodType: String,
    val targetValue: Int,
    val platformId: Int,
    val languageId: Int,
    val autoRenew: Boolean
)
