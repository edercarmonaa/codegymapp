package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeRescheduleRequestDto
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import retrofit2.HttpException

class TodayRepository(private val api: CodeGymApi) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun today(): Result<TodayData> = runCatching {
        val response = api.mobileToday()
        if (!response.ok) {
            error(response.message ?: "No se pudo cargar Mi día.")
        }

        TodayData(
            today = response.today.map { it.toDomain() },
            expired = response.expired.map { it.toDomain() }
        )
    }

    suspend fun completeChallenge(id: Int): Result<String> =
        challengeAction { api.completeChallenge(MobileChallengeActionRequestDto(id)) }

    suspend fun missChallenge(id: Int): Result<String> =
        challengeAction { api.missChallenge(MobileChallengeActionRequestDto(id)) }

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
            }
        }
}

data class TodayData(
    val today: List<MobileChallenge>,
    val expired: List<MobileChallenge>
)
