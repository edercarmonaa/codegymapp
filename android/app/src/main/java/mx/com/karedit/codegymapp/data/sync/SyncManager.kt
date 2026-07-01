package mx.com.karedit.codegymapp.data.sync

import com.squareup.moshi.Moshi
import java.util.concurrent.atomic.AtomicBoolean
import mx.com.karedit.codegymapp.data.local.dao.PendingActionDao
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeRescheduleRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileGoalCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileGoalUpdateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileManualChallengeRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileNotificationActionRequestDto

class SyncManager(
    private val api: CodeGymApi,
    private val pendingActionDao: PendingActionDao,
    private val moshi: Moshi
) {
    private val isSyncing = AtomicBoolean(false)

    suspend fun syncNow() {
        if (!isSyncing.compareAndSet(false, true)) {
            return
        }

        try {
            pendingActionDao.pending().forEach { action ->
                try {
                    val ok = execute(action.type, action.payloadJson)
                    if (ok) {
                        pendingActionDao.delete(action)
                    } else {
                        pendingActionDao.markFailed(action.id, "El servidor no aceptó la acción.")
                    }
                } catch (exception: Exception) {
                    pendingActionDao.markFailed(action.id, exception.message ?: "No se pudo sincronizar.")
                    return
                }
            }
        } finally {
            isSyncing.set(false)
        }
    }

    private suspend fun execute(type: String, payloadJson: String): Boolean =
        when (type) {
            ActionTypes.CHALLENGE_CREATE -> {
                val payload = moshi.adapter(ChallengeCreatePayload::class.java).fromJson(payloadJson) ?: return false
                val response = api.storeChallenge(
                    MobileChallengeCreateRequestDto(payload.platformId, payload.scheduledDate)
                )
                val serverId = response.id
                if (!response.ok) {
                    false
                } else if (serverId == null || !payload.hasDetails()) {
                    true
                } else {
                    api.saveChallengeDetails(
                        mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeDetailsRequestDto(
                            id = serverId,
                            platformId = payload.platformId,
                            title = payload.title,
                            challengeUrl = payload.challengeUrl,
                            difficulty = payload.difficulty,
                            timeSpentMinutes = payload.timeSpentMinutes,
                            notes = payload.notes,
                            languageIds = payload.languageIds,
                            githubLinks = payload.githubLinks
                        )
                    ).ok
                }
            }
            ActionTypes.CHALLENGE_MANUAL_CREATE -> {
                val payload = moshi.adapter(ManualChallengePayload::class.java).fromJson(payloadJson) ?: return false
                api.storeManualChallenge(
                    MobileManualChallengeRequestDto(
                        platformId = payload.platformId,
                        title = payload.title,
                        challengeUrl = payload.challengeUrl,
                        difficulty = payload.difficulty,
                        timeSpentMinutes = payload.timeSpentMinutes,
                        notes = payload.notes,
                        languageIds = payload.languageIds,
                        githubLinks = payload.githubLinks
                    )
                ).ok
            }
            ActionTypes.CHALLENGE_COMPLETE -> {
                val payload = idPayload(payloadJson)
                api.completeChallenge(MobileChallengeActionRequestDto(payload.id)).ok
            }
            ActionTypes.CHALLENGE_MISS -> {
                val payload = idPayload(payloadJson)
                api.missChallenge(MobileChallengeActionRequestDto(payload.id)).ok
            }
            ActionTypes.CHALLENGE_CANCEL -> {
                val payload = idPayload(payloadJson)
                api.cancelChallenge(MobileChallengeActionRequestDto(payload.id)).ok
            }
            ActionTypes.CHALLENGE_RESCHEDULE -> {
                val payload = moshi.adapter(ReschedulePayload::class.java).fromJson(payloadJson) ?: return false
                api.rescheduleChallenge(
                    MobileChallengeRescheduleRequestDto(payload.id, payload.scheduledDate)
                ).ok
            }
            ActionTypes.NOTIFICATION_MARK_READ -> {
                val payload = idPayload(payloadJson)
                api.markNotificationRead(MobileNotificationActionRequestDto(payload.id)).ok
            }
            ActionTypes.NOTIFICATION_DELETE -> {
                val payload = idPayload(payloadJson)
                api.deleteNotification(MobileNotificationActionRequestDto(payload.id)).ok
            }
            ActionTypes.GOAL_CREATE -> {
                val payload = goalPayload(payloadJson)
                api.storeGoal(
                    MobileGoalCreateRequestDto(
                        goalType = payload.goalType,
                        periodType = payload.periodType,
                        targetValue = payload.targetValue,
                        platformId = payload.platformId,
                        languageId = payload.languageId,
                        autoRenew = payload.autoRenew
                    )
                ).ok
            }
            ActionTypes.GOAL_UPDATE -> {
                val payload = goalPayload(payloadJson)
                api.updateGoal(
                    MobileGoalUpdateRequestDto(
                        id = payload.id,
                        goalType = payload.goalType,
                        periodType = payload.periodType,
                        targetValue = payload.targetValue,
                        platformId = payload.platformId,
                        languageId = payload.languageId,
                        autoRenew = payload.autoRenew
                    )
                ).ok
            }
            else -> true
        }

    private fun idPayload(payloadJson: String): IdPayload =
        moshi.adapter(IdPayload::class.java).fromJson(payloadJson) ?: error("Acción inválida.")

    private fun goalPayload(payloadJson: String): GoalPayload =
        moshi.adapter(GoalPayload::class.java).fromJson(payloadJson) ?: error("Meta inválida.")
}

private fun ChallengeCreatePayload.hasDetails(): Boolean =
    title.isNotBlank() ||
        challengeUrl.isNotBlank() ||
        difficulty.isNotBlank() ||
        (timeSpentMinutes ?: 0) > 0 ||
        notes.isNotBlank() ||
        languageIds.isNotEmpty() ||
        githubLinks.isNotBlank()
