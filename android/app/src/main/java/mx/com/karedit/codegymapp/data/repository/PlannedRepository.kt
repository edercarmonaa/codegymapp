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
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import retrofit2.HttpException

class PlannedRepository(
    private val api: CodeGymApi,
    private val challengeDao: CachedChallengeDao
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun planned(): Result<List<MobileChallenge>> = runCatching {
        try {
            val response = api.mobilePlanned()
            if (!response.ok) {
                error(response.message ?: "No se pudo cargar Planeado.")
            }

            val planned = response.planned.map { it.toDomain() }
            challengeDao.replaceSection(SECTION_PLANNED, planned.map { it.toCacheEntity(SECTION_PLANNED) })
            planned
        } catch (exception: Exception) {
            val cached = challengeDao.getBySection(SECTION_PLANNED).map { it.toDomain() }
            if (cached.isNotEmpty()) cached else throw exception.toOfflineReadException("Planeado")
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
            error(OFFLINE_ACTION_MESSAGE)
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
        }
    }

    suspend fun cancelChallenge(id: Int): Result<String> =
        challengeAction { api.cancelChallenge(MobileChallengeActionRequestDto(id)) }

    suspend fun rescheduleChallenge(id: Int, scheduledDate: String): Result<String> =
        challengeAction { api.rescheduleChallenge(MobileChallengeRescheduleRequestDto(id, scheduledDate)) }

    private suspend fun challengeAction(action: suspend () -> MobileActionResponseDto): Result<String> =
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
                error(OFFLINE_ACTION_MESSAGE)
            }
        }
}

private const val SECTION_PLANNED = "planned"
