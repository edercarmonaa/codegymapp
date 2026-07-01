package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.local.dao.CachedCatalogDao
import mx.com.karedit.codegymapp.data.local.mapper.toCacheEntity
import mx.com.karedit.codegymapp.data.local.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileRoutineCreateRequestDto
import mx.com.karedit.codegymapp.domain.model.MobilePlatform
import retrofit2.HttpException

class CreateRoutineRepository(
    private val api: CodeGymApi,
    private val catalogDao: CachedCatalogDao
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun platforms(): Result<List<MobilePlatform>> = runCatching {
        try {
            val response = api.mobileCreateOptions()
            if (!response.ok) {
                error(response.message ?: "No se pudieron cargar las plataformas.")
            }

            val now = System.currentTimeMillis()
            catalogDao.replacePlatforms(response.platforms.map { it.toCacheEntity(now) })
            catalogDao.replaceLanguages(response.languages.map { it.toCacheEntity(now) })
            response.platforms.map { it.toDomain() }
        } catch (exception: Exception) {
            val cached = catalogDao.platforms().map { it.toDomain() }
            if (cached.isNotEmpty()) cached else throw exception.toOfflineReadException("las plataformas")
        }
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
