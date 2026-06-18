package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileRoutineCreateRequestDto
import mx.com.karedit.codegymapp.domain.model.MobilePlatform
import retrofit2.HttpException

class CreateRoutineRepository(private val api: CodeGymApi) {
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

    suspend fun create(
        platformId: Int,
        frequencyType: String,
        weekDays: List<Int>,
        monthDay: Int,
        startDate: String,
        endDate: String
    ): Result<String> = runCatching {
        try {
            val response = api.storeRoutine(
                MobileRoutineCreateRequestDto(
                    platformId = platformId,
                    frequencyType = frequencyType,
                    weekDays = weekDays,
                    monthDay = monthDay,
                    startDate = startDate,
                    endDate = endDate
                )
            )
            if (!response.ok) {
                error(response.message ?: "No se pudo crear la rutina.")
            }

            response.message ?: "Rutina creada."
        } catch (exception: HttpException) {
            val apiMessage = exception.response()
                ?.errorBody()
                ?.string()
                ?.let { body -> errorAdapter.fromJson(body)?.message }

            error(apiMessage ?: "No se pudo crear la rutina.")
        }
    }
}
