package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.local.dao.CachedCatalogDao
import mx.com.karedit.codegymapp.data.local.mapper.toCacheEntity
import mx.com.karedit.codegymapp.data.local.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeDetailsRequestDto
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform
import retrofit2.HttpException

class ChallengeDetailsRepository(
    private val api: CodeGymApi,
    private val catalogDao: CachedCatalogDao
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun save(
        id: Int,
        platformId: Int,
        title: String,
        challengeUrl: String,
        difficulty: String,
        timeSpentMinutes: Int?,
        notes: String,
        languageIds: List<Int>,
        githubLinks: String
    ): Result<String> = runCatching {
        try {
            val response = api.saveChallengeDetails(
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
        } catch (exception: java.io.IOException) {
            error(OFFLINE_ACTION_MESSAGE)
        }
    }

    suspend fun options(): Result<ChallengeDetailsOptions> = runCatching {
        try {
            val response = api.mobileCreateOptions()
            if (!response.ok) {
                error(response.message ?: "No se pudieron cargar los catálogos.")
            }

            val now = System.currentTimeMillis()
            catalogDao.replacePlatforms(response.platforms.map { it.toCacheEntity(now) })
            catalogDao.replaceLanguages(response.languages.map { it.toCacheEntity(now) })
            ChallengeDetailsOptions(
                platforms = response.platforms.map { it.toDomain() },
                languages = response.languages.map { it.toDomain() }
            )
        } catch (exception: Exception) {
            val cached = ChallengeDetailsOptions(
                platforms = catalogDao.platforms().map { it.toDomain() },
                languages = catalogDao.languages().map { it.toDomain() }
            )
            if (cached.platforms.isNotEmpty() || cached.languages.isNotEmpty()) {
                cached
            } else {
                throw exception.toOfflineReadException("los catálogos del detalle")
            }
        }
    }
}

data class ChallengeDetailsOptions(
    val platforms: List<MobilePlatform>,
    val languages: List<MobileLanguage>
)
