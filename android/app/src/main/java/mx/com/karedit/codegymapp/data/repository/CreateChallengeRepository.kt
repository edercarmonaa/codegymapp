package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeCreateRequestDto
import mx.com.karedit.codegymapp.domain.model.MobilePlatform
import retrofit2.HttpException

class CreateChallengeRepository(private val api: CodeGymApi) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun platforms(): Result<List<MobilePlatform>> = runCatching {
        val response = api.mobileCreateOptions()
        if (!response.ok) {
            error(response.message ?: "No se pudieron cargar las plataformas.")
        }

        response.platforms.map { MobilePlatform(id = it.id, name = it.name) }
    }

    suspend fun create(platformId: Int, scheduledDate: String): Result<String> = runCatching {
        try {
            val response = api.storeChallenge(
                MobileChallengeCreateRequestDto(
                    platformId = platformId,
                    scheduledDate = scheduledDate
                )
            )
            if (!response.ok) {
                error(response.message ?: "No se pudo crear el reto.")
            }

            response.message ?: "Reto creado."
        } catch (exception: HttpException) {
            val apiMessage = exception.response()
                ?.errorBody()
                ?.string()
                ?.let { body -> errorAdapter.fromJson(body)?.message }

            error(apiMessage ?: "No se pudo crear el reto.")
        }
    }
}
