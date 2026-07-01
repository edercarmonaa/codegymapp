package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.local.dao.CachedChallengeDao
import mx.com.karedit.codegymapp.data.local.mapper.toCacheEntity
import mx.com.karedit.codegymapp.data.local.mapper.toDomain
import mx.com.karedit.codegymapp.data.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeRescheduleRequestDto
import mx.com.karedit.codegymapp.data.sync.ActionTypes
import mx.com.karedit.codegymapp.data.sync.OfflineActionQueue
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import java.time.LocalDate
import retrofit2.HttpException

class ChallengesRepository(
    private val api: CodeGymApi,
    private val challengeDao: CachedChallengeDao,
    private val offlineActionQueue: OfflineActionQueue
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun challenges(month: String, status: String): Result<List<MobileChallenge>> = runCatching {
        val section = "challenges:$month:$status"
        try {
            val response = api.mobileChallenges(month = month, status = status)
            if (!response.ok) {
                error(response.message ?: "No se pudieron cargar los retos.")
            }

            val challenges = response.challenges.map { it.toDomain() }
            challengeDao.replaceSection(section, challenges.map { it.toCacheEntity(section) })
            challenges
        } catch (exception: Exception) {
            val cached = challengeDao.getBySection(section).map { it.toDomain() }
            if (cached.isNotEmpty()) cached else throw exception.toOfflineReadException("Retos")
        }
    }

    suspend fun completeChallenge(id: Int): Result<String> = runCatching {
        try {
            val response = api.completeChallenge(MobileChallengeActionRequestDto(id))
            if (!response.ok) {
                error(response.message ?: "No se pudo actualizar el reto.")
            }

            response.message ?: "Reto actualizado."
        } catch (exception: HttpException) {
            val apiMessage = exception.response()
                ?.errorBody()
                ?.string()
                ?.let { body -> errorAdapter.fromJson(body)?.message }

            error(apiMessage ?: "No se pudo actualizar el reto.")
        } catch (exception: java.io.IOException) {
            offlineActionQueue.enqueueChallengeAction(ActionTypes.CHALLENGE_COMPLETE, id)
            challengeDao.updateStatus(id, "completed", LocalDate.now().toString())
            "Acción guardada para sincronizar."
        }
    }

    suspend fun missChallenge(id: Int): Result<String> = runCatching {
        try {
            val response = api.missChallenge(MobileChallengeActionRequestDto(id))
            if (!response.ok) {
                error(response.message ?: "No se pudo actualizar el reto.")
            }

            response.message ?: "Reto actualizado."
        } catch (exception: HttpException) {
            val apiMessage = exception.response()
                ?.errorBody()
                ?.string()
                ?.let { body -> errorAdapter.fromJson(body)?.message }

            error(apiMessage ?: "No se pudo actualizar el reto.")
        } catch (exception: java.io.IOException) {
            offlineActionQueue.enqueueChallengeAction(ActionTypes.CHALLENGE_MISS, id)
            challengeDao.updateStatus(id, "missed", null)
            "Acción guardada para sincronizar."
        }
    }

    suspend fun cancelChallenge(id: Int): Result<String> =
        challengeAction(
            offline = {
                offlineActionQueue.enqueueChallengeAction(ActionTypes.CHALLENGE_CANCEL, id)
                challengeDao.updateStatus(id, "cancelled", null)
            }
        ) { api.cancelChallenge(MobileChallengeActionRequestDto(id)) }

    suspend fun rescheduleChallenge(id: Int, scheduledDate: String): Result<String> =
        challengeAction(
            offline = {
                offlineActionQueue.enqueueReschedule(id, scheduledDate)
                challengeDao.updateScheduledDate(id, scheduledDate)
            }
        ) { api.rescheduleChallenge(MobileChallengeRescheduleRequestDto(id, scheduledDate)) }

    private suspend fun challengeAction(
        offline: suspend () -> Unit,
        action: suspend () -> MobileActionResponseDto
    ): Result<String> =
        runCatching {
            try {
                val response = action()
                if (!response.ok) {
                    error(response.message ?: "No se pudo actualizar el reto.")
                }

                response.message ?: "Reto actualizado."
            } catch (exception: HttpException) {
                val apiMessage = exception.response()
                    ?.errorBody()
                    ?.string()
                    ?.let { body -> errorAdapter.fromJson(body)?.message }

                error(apiMessage ?: "No se pudo actualizar el reto.")
            } catch (exception: java.io.IOException) {
                offline()
                "Acción guardada para sincronizar."
            }
        }
}
