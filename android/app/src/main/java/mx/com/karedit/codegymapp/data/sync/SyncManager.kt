package mx.com.karedit.codegymapp.data.sync

import com.squareup.moshi.Moshi
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import mx.com.karedit.codegymapp.data.local.dao.CachedChallengeDao
import mx.com.karedit.codegymapp.data.local.dao.PendingActionDao
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeDetailsRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeRescheduleRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileGoalCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileGoalUpdateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileManualChallengeRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileNotificationActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileRoutineCreateRequestDto
import retrofit2.HttpException

class SyncManager(
    private val api: CodeGymApi,
    private val pendingActionDao: PendingActionDao,
    private val challengeDao: CachedChallengeDao,
    private val moshi: Moshi
) {
    private val isSyncing = AtomicBoolean(false)

    suspend fun syncNow() {
        if (!isSyncing.compareAndSet(false, true)) {
            return
        }

        try {
            for (action in pendingActionDao.pending()) {
                if (action.errorKind == SyncErrorKinds.VALIDATION) {
                    continue
                }
                val now = System.currentTimeMillis()
                if (action.nextAttemptAt > now) {
                    return
                }
                try {
                    val ok = execute(action.type, action.payloadJson)
                    if (ok) {
                        pendingActionDao.delete(action)
                    } else {
                        markFailed(
                            action = action,
                            failure = SyncFailure.validation("El servidor no aceptó la acción."),
                            attemptedAt = now
                        )
                    }
                } catch (exception: Exception) {
                    val failure = classifySyncFailure(exception)
                    markFailed(action, failure, now)
                    if (failure.stopsQueue) return
                }
            }
        } finally {
            isSyncing.set(false)
        }
    }

    private suspend fun markFailed(
        action: PendingActionEntity,
        failure: SyncFailure,
        attemptedAt: Long
    ) {
        pendingActionDao.markFailed(
            id = action.id,
            message = failure.message,
            errorKind = failure.kind,
            attemptedAt = attemptedAt,
            nextAttemptAt = if (failure.retryable) {
                attemptedAt + retryDelayMillis(action.attempts + 1)
            } else {
                0
            }
        )
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
                } else if (serverId == null) {
                    false
                } else {
                    remapChallengeId(payload.localId, serverId)
                    if (!payload.hasDetails()) {
                        true
                    } else {
                        val details = payload.toDetails(serverId)
                        val detailsSaved = runCatching {
                            api.saveChallengeDetails(details.toRequest()).ok
                        }.getOrDefault(false)
                        if (!detailsSaved) {
                            enqueueChallengeDetails(details)
                        }
                        true
                    }
                }
            }
            ActionTypes.CHALLENGE_SAVE_DETAILS -> {
                val payload = moshi.adapter(ChallengeDetailsPayload::class.java).fromJson(payloadJson) ?: return false
                api.saveChallengeDetails(
                    MobileChallengeDetailsRequestDto(
                        id = payload.id,
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
            ActionTypes.ROUTINE_CREATE -> {
                val payload = moshi.adapter(RoutinePayload::class.java).fromJson(payloadJson) ?: return false
                api.storeRoutine(
                    MobileRoutineCreateRequestDto(
                        platformId = payload.platformId,
                        frequencyType = payload.frequencyType,
                        weekDays = payload.weekDays,
                        monthDay = payload.monthDay,
                        startDate = payload.startDate,
                        endDate = payload.endDate
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
            else -> false
        }

    private suspend fun remapChallengeId(localId: Int, serverId: Int) {
        if (localId >= 0 || localId == serverId) return
        challengeDao.replaceLocalId(localId, serverId)
        pendingActionDao.pending().forEach { action ->
            val updatedPayload = remapPendingChallengePayload(
                type = action.type,
                payloadJson = action.payloadJson,
                localId = localId,
                serverId = serverId,
                moshi = moshi
            )
            if (updatedPayload != null) {
                pendingActionDao.updatePayload(action.id, updatedPayload)
            }
        }
    }

    private suspend fun enqueueChallengeDetails(payload: ChallengeDetailsPayload) {
        pendingActionDao.insert(
            PendingActionEntity(
                type = ActionTypes.CHALLENGE_SAVE_DETAILS,
                payloadJson = moshi.adapter(ChallengeDetailsPayload::class.java).toJson(payload),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    private fun idPayload(payloadJson: String): IdPayload =
        moshi.adapter(IdPayload::class.java).fromJson(payloadJson) ?: error("Acción inválida.")

    private fun goalPayload(payloadJson: String): GoalPayload =
        moshi.adapter(GoalPayload::class.java).fromJson(payloadJson) ?: error("Meta inválida.")
}

internal data class SyncFailure(
    val kind: String,
    val message: String,
    val retryable: Boolean,
    val stopsQueue: Boolean
) {
    companion object {
        fun validation(message: String) = SyncFailure(
            kind = SyncErrorKinds.VALIDATION,
            message = message,
            retryable = false,
            stopsQueue = false
        )
    }
}

internal object SyncErrorKinds {
    const val NETWORK = "network"
    const val SERVER = "server"
    const val AUTH = "auth"
    const val VALIDATION = "validation"
    const val UNKNOWN = "unknown"
}

internal fun classifySyncFailure(exception: Exception): SyncFailure = when {
    exception is IOException -> SyncFailure(
        SyncErrorKinds.NETWORK,
        "Sin conexión para sincronizar.",
        retryable = true,
        stopsQueue = true
    )
    exception is HttpException && exception.code() == 401 -> SyncFailure(
        SyncErrorKinds.AUTH,
        "La sesión expiró durante la sincronización.",
        retryable = false,
        stopsQueue = true
    )
    exception is HttpException && exception.code() in 400..499 &&
        exception.code() !in setOf(408, 429) -> SyncFailure.validation(
        "La acción requiere revisión antes de sincronizar."
    )
    exception is HttpException && (exception.code() >= 500 || exception.code() in setOf(408, 429)) -> SyncFailure(
        SyncErrorKinds.SERVER,
        "El servidor no está disponible temporalmente.",
        retryable = true,
        stopsQueue = true
    )
    exception is IllegalArgumentException || exception is IllegalStateException -> SyncFailure.validation(
        "Los datos guardados para esta acción no son válidos."
    )
    else -> SyncFailure(
        SyncErrorKinds.UNKNOWN,
        "No se pudo sincronizar la acción.",
        retryable = true,
        stopsQueue = true
    )
}

internal fun retryDelayMillis(attempt: Int): Long {
    val boundedAttempt = attempt.coerceIn(1, 14)
    return (60_000L shl (boundedAttempt - 1)).coerceAtMost(6 * 60 * 60 * 1_000L)
}

private fun ChallengeCreatePayload.hasDetails(): Boolean =
    title.isNotBlank() ||
        challengeUrl.isNotBlank() ||
        difficulty.isNotBlank() ||
        (timeSpentMinutes ?: 0) > 0 ||
        notes.isNotBlank() ||
        languageIds.isNotEmpty() ||
        githubLinks.isNotBlank()

private fun ChallengeCreatePayload.toDetails(serverId: Int): ChallengeDetailsPayload =
    ChallengeDetailsPayload(
        id = serverId,
        platformId = platformId,
        title = title,
        challengeUrl = challengeUrl,
        difficulty = difficulty,
        timeSpentMinutes = timeSpentMinutes,
        notes = notes,
        languageIds = languageIds,
        githubLinks = githubLinks
    )

private fun ChallengeDetailsPayload.toRequest(): MobileChallengeDetailsRequestDto =
    MobileChallengeDetailsRequestDto(
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

internal fun remapPendingChallengePayload(
    type: String,
    payloadJson: String,
    localId: Int,
    serverId: Int,
    moshi: Moshi
): String? = when (type) {
    ActionTypes.CHALLENGE_SAVE_DETAILS -> {
        val adapter = moshi.adapter(ChallengeDetailsPayload::class.java)
        adapter.fromJson(payloadJson)
            ?.takeIf { it.id == localId }
            ?.copy(id = serverId)
            ?.let(adapter::toJson)
    }
    ActionTypes.CHALLENGE_COMPLETE,
    ActionTypes.CHALLENGE_MISS,
    ActionTypes.CHALLENGE_CANCEL -> {
        val adapter = moshi.adapter(IdPayload::class.java)
        adapter.fromJson(payloadJson)
            ?.takeIf { it.id == localId }
            ?.copy(id = serverId)
            ?.let(adapter::toJson)
    }
    ActionTypes.CHALLENGE_RESCHEDULE -> {
        val adapter = moshi.adapter(ReschedulePayload::class.java)
        adapter.fromJson(payloadJson)
            ?.takeIf { it.id == localId }
            ?.copy(id = serverId)
            ?.let(adapter::toJson)
    }
    else -> null
}
