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
        if (type in ActionTypes.CHALLENGE_TERMINAL_ACTIONS) {
            pendingActionDao.deleteByTypesAndPayloadPattern(
                types = ActionTypes.CHALLENGE_TERMINAL_ACTIONS,
                payloadPattern = idPattern(id)
            )
        }
        enqueue(type, IdPayload(id))
    }

    suspend fun enqueueChallengeDetails(
        id: Int,
        platformId: Int,
        title: String,
        challengeUrl: String,
        difficulty: String,
        timeSpentMinutes: Int?,
        notes: String,
        languageIds: List<Int>,
        githubLinks: String
    ) {
        val payload = ChallengeDetailsPayload(
            id = id,
            platformId = platformId,
            title = title,
            challengeUrl = challengeUrl,
            difficulty = difficulty,
            timeSpentMinutes = timeSpentMinutes,
            notes = notes,
            languageIds = languageIds,
            githubLinks = githubLinks
        )
        val existing = pendingActionDao.findByTypeAndPayloadPattern(
            type = ActionTypes.CHALLENGE_SAVE_DETAILS,
            payloadPattern = "%\"id\":$id%"
        )
        if (existing != null) {
            pendingActionDao.updatePayload(
                id = existing.id,
                payloadJson = moshi.adapter(ChallengeDetailsPayload::class.java).toJson(payload)
            )
            return
        }

        enqueue(ActionTypes.CHALLENGE_SAVE_DETAILS, payload)
    }

    suspend fun enqueueChallengeCreate(localId: Int, platformId: Int, scheduledDate: String) {
        enqueue(
            ActionTypes.CHALLENGE_CREATE,
            ChallengeCreatePayload(localId = localId, platformId = platformId, scheduledDate = scheduledDate)
        )
    }

    suspend fun updateChallengeCreateDetails(
        localId: Int,
        platformId: Int,
        scheduledDate: String,
        title: String,
        challengeUrl: String,
        difficulty: String,
        timeSpentMinutes: Int?,
        notes: String,
        languageIds: List<Int>,
        githubLinks: String
    ) {
        val action = pendingActionDao.findByTypeAndPayloadPattern(
            type = ActionTypes.CHALLENGE_CREATE,
            payloadPattern = "%\"localId\":$localId%"
        ) ?: return
        val current = moshi.adapter(ChallengeCreatePayload::class.java).fromJson(action.payloadJson) ?: return
        pendingActionDao.updatePayload(
            id = action.id,
            payloadJson = moshi.adapter(ChallengeCreatePayload::class.java).toJson(
                current.copy(
                    platformId = platformId,
                    scheduledDate = scheduledDate.ifBlank { current.scheduledDate },
                    title = title,
                    challengeUrl = challengeUrl,
                    difficulty = difficulty,
                    timeSpentMinutes = timeSpentMinutes,
                    notes = notes,
                    languageIds = languageIds,
                    githubLinks = githubLinks
                )
            )
        )
    }

    suspend fun enqueueManualChallengeCreate(
        platformId: Int,
        title: String,
        challengeUrl: String,
        difficulty: String,
        timeSpentMinutes: Int,
        notes: String,
        languageIds: List<Int>,
        githubLinks: String
    ) {
        enqueue(
            ActionTypes.CHALLENGE_MANUAL_CREATE,
            ManualChallengePayload(
                platformId = platformId,
                title = title,
                challengeUrl = challengeUrl,
                difficulty = difficulty,
                timeSpentMinutes = timeSpentMinutes,
                notes = notes,
                languageIds = languageIds,
                githubLinks = githubLinks
            )
        )
    }

    suspend fun enqueueRoutineCreate(
        platformId: Int,
        frequencyType: String,
        weekDays: List<Int>,
        monthDay: Int,
        startDate: String,
        endDate: String
    ) {
        enqueue(
            ActionTypes.ROUTINE_CREATE,
            RoutinePayload(
                platformId = platformId,
                frequencyType = frequencyType,
                weekDays = weekDays,
                monthDay = monthDay,
                startDate = startDate,
                endDate = endDate
            )
        )
    }

    suspend fun enqueueReschedule(id: Int, scheduledDate: String) {
        val payload = ReschedulePayload(id, scheduledDate)
        val existing = pendingActionDao.findByTypeAndPayloadPattern(
            type = ActionTypes.CHALLENGE_RESCHEDULE,
            payloadPattern = idPattern(id)
        )
        if (existing != null) {
            pendingActionDao.updatePayload(
                existing.id,
                moshi.adapter(ReschedulePayload::class.java).toJson(payload)
            )
        } else {
            enqueue(ActionTypes.CHALLENGE_RESCHEDULE, payload)
        }
    }

    suspend fun enqueueNotificationAction(type: String, id: Int) {
        val pattern = idPattern(id)
        if (type == ActionTypes.NOTIFICATION_DELETE) {
            pendingActionDao.deleteByTypesAndPayloadPattern(
                types = listOf(ActionTypes.NOTIFICATION_MARK_READ, ActionTypes.NOTIFICATION_DELETE),
                payloadPattern = pattern
            )
        } else if (pendingActionDao.findByTypeAndPayloadPattern(type, pattern) != null) {
            return
        }
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
        val payload = GoalPayload(id, goalType, periodType, targetValue, platformId, languageId, autoRenew)
        val existing = pendingActionDao.findByTypeAndPayloadPattern(
            type = ActionTypes.GOAL_UPDATE,
            payloadPattern = idPattern(id)
        )
        if (existing != null) {
            pendingActionDao.updatePayload(
                existing.id,
                moshi.adapter(GoalPayload::class.java).toJson(payload)
            )
        } else {
            enqueue(ActionTypes.GOAL_UPDATE, payload)
        }
    }

    private fun idPattern(id: Int): String = "%\"id\":$id%"

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
    const val CHALLENGE_CREATE = "challenge.create"
    const val CHALLENGE_MANUAL_CREATE = "challenge.manual_create"
    const val CHALLENGE_SAVE_DETAILS = "challenge.save_details"
    const val CHALLENGE_COMPLETE = "challenge.complete"
    const val CHALLENGE_MISS = "challenge.miss"
    const val CHALLENGE_CANCEL = "challenge.cancel"
    const val CHALLENGE_RESCHEDULE = "challenge.reschedule"
    const val NOTIFICATION_MARK_READ = "notification.mark_read"
    const val NOTIFICATION_DELETE = "notification.delete"
    const val GOAL_CREATE = "goal.create"
    const val GOAL_UPDATE = "goal.update"
    const val ROUTINE_CREATE = "routine.create"

    val CHALLENGE_TERMINAL_ACTIONS = listOf(CHALLENGE_COMPLETE, CHALLENGE_MISS, CHALLENGE_CANCEL)
}

data class IdPayload(val id: Int)
data class ChallengeCreatePayload(
    val localId: Int = 0,
    val platformId: Int,
    val scheduledDate: String,
    val title: String = "",
    val challengeUrl: String = "",
    val difficulty: String = "",
    val timeSpentMinutes: Int? = null,
    val notes: String = "",
    val languageIds: List<Int> = emptyList(),
    val githubLinks: String = ""
)
data class ChallengeDetailsPayload(
    val id: Int,
    val platformId: Int,
    val title: String,
    val challengeUrl: String,
    val difficulty: String,
    val timeSpentMinutes: Int?,
    val notes: String,
    val languageIds: List<Int>,
    val githubLinks: String
)
data class ManualChallengePayload(
    val platformId: Int,
    val title: String,
    val challengeUrl: String,
    val difficulty: String,
    val timeSpentMinutes: Int,
    val notes: String,
    val languageIds: List<Int>,
    val githubLinks: String
)
data class ReschedulePayload(val id: Int, val scheduledDate: String)
data class RoutinePayload(
    val platformId: Int,
    val frequencyType: String,
    val weekDays: List<Int>,
    val monthDay: Int,
    val startDate: String,
    val endDate: String
)
data class GoalPayload(
    val id: Int,
    val goalType: String,
    val periodType: String,
    val targetValue: Int,
    val platformId: Int,
    val languageId: Int,
    val autoRenew: Boolean
)
