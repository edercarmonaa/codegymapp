package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import mx.com.karedit.codegymapp.data.local.dao.CachedCatalogDao
import mx.com.karedit.codegymapp.data.local.mapper.toCacheEntity
import mx.com.karedit.codegymapp.data.local.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileManualChallengeRequestDto
import mx.com.karedit.codegymapp.data.sync.OfflineActionQueue
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform
import retrofit2.HttpException

class CreateChallengeRepository(
    private val api: CodeGymApi,
    private val catalogDao: CachedCatalogDao,
    private val offlineActionQueue: OfflineActionQueue
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

            cacheCatalogs(response.platforms, response.languages)
            response.platforms.map { it.toDomain() }
        } catch (exception: Exception) {
            val cached = catalogDao.platforms().map { it.toDomain() }
            if (cached.isNotEmpty()) cached else throw exception.toOfflineReadException("las plataformas")
        }
    }

    suspend fun options(): Result<MobileChallengeOptions> = runCatching {
        try {
            val response = api.mobileCreateOptions()
            if (!response.ok) {
                error(response.message ?: "No se pudieron cargar los catálogos.")
            }

            cacheCatalogs(response.platforms, response.languages)
            MobileChallengeOptions(
                platforms = response.platforms.map { it.toDomain() },
                languages = response.languages.map { it.toDomain() }
            )
        } catch (exception: Exception) {
            val cached = cachedOptions()
            if (cached.platforms.isNotEmpty() || cached.languages.isNotEmpty()) {
                cached
            } else {
                throw exception.toOfflineReadException("los catálogos")
            }
        }
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
        } catch (exception: IOException) {
            offlineActionQueue.enqueueChallengeCreate(platformId, scheduledDate)
            "Reto guardado para sincronizar."
        }
    }

    suspend fun createManual(
        platformId: Int,
        title: String,
        challengeUrl: String,
        difficulty: String,
        timeSpentMinutes: Int,
        notes: String,
        languageIds: List<Int>,
        githubLinks: String
    ): Result<String> = runCatching {
        try {
            val response = api.storeManualChallenge(
                MobileManualChallengeRequestDto(
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
            if (!response.ok) {
                error(response.message ?: "No se pudo registrar el reto realizado.")
            }

            response.message ?: "Reto realizado registrado."
        } catch (exception: HttpException) {
            val apiMessage = exception.response()
                ?.errorBody()
                ?.string()
                ?.let { body -> errorAdapter.fromJson(body)?.message }

            error(apiMessage ?: "No se pudo registrar el reto realizado.")
        } catch (exception: IOException) {
            offlineActionQueue.enqueueManualChallengeCreate(
                platformId = platformId,
                title = title,
                challengeUrl = challengeUrl,
                difficulty = difficulty,
                timeSpentMinutes = timeSpentMinutes,
                notes = notes,
                languageIds = languageIds,
                githubLinks = githubLinks
            )
            "Reto realizado guardado para sincronizar."
        }
    }

    private suspend fun cacheCatalogs(
        platforms: List<mx.com.karedit.codegymapp.data.remote.dto.MobilePlatformDto>,
        languages: List<mx.com.karedit.codegymapp.data.remote.dto.MobileLanguageDto>
    ) {
        val now = System.currentTimeMillis()
        catalogDao.replacePlatforms(platforms.map { it.toCacheEntity(now) })
        catalogDao.replaceLanguages(languages.map { it.toCacheEntity(now) })
    }

    private suspend fun cachedOptions(): MobileChallengeOptions =
        MobileChallengeOptions(
            platforms = catalogDao.platforms().map { it.toDomain() },
            languages = catalogDao.languages().map { it.toDomain() }
        )
}

data class MobileChallengeOptions(
    val platforms: List<MobilePlatform>,
    val languages: List<MobileLanguage>
)
