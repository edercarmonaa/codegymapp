package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeDetailsRequestDto
import retrofit2.HttpException

class ChallengeDetailsRepository(private val api: CodeGymApi) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun save(
        id: Int,
        title: String,
        challengeUrl: String,
        difficulty: String,
        timeSpentMinutes: Int?,
        notes: String
    ): Result<String> = runCatching {
        try {
            val response = api.saveChallengeDetails(
                MobileChallengeDetailsRequestDto(
                    id = id,
                    title = title,
                    challengeUrl = challengeUrl,
                    difficulty = difficulty,
                    timeSpentMinutes = timeSpentMinutes,
                    notes = notes
                )
            )
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
