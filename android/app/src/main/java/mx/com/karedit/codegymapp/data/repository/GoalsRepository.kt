package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.local.dao.CachedGoalDao
import mx.com.karedit.codegymapp.data.local.mapper.toCacheEntity
import mx.com.karedit.codegymapp.data.local.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileGoalCreateRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileGoalUpdateRequestDto
import mx.com.karedit.codegymapp.domain.model.MobileGoal
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform
import java.io.IOException
import retrofit2.HttpException

class GoalsRepository(
    private val api: CodeGymApi,
    private val goalDao: CachedGoalDao
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MobileActionResponseDto::class.java)

    suspend fun options(): Result<MobileGoalOptions> = runCatching {
        val response = api.mobileGoalOptions()
        if (!response.ok) {
            error(response.message ?: "No se pudieron cargar las opciones de metas.")
        }

        MobileGoalOptions(
            goalTypes = response.goalTypes.map { MobileGoalOption(it.key, it.value) },
            periodTypes = response.periodTypes.map { MobileGoalOption(it.key, it.value) },
            platforms = response.platforms.map { MobilePlatform(id = it.id, name = it.name) },
            languages = response.languages.map { MobileLanguage(id = it.id, name = it.name) }
        )
    }

    suspend fun activeGoals(): Result<List<MobileGoal>> = runCatching {
        try {
            val response = api.mobileGoals()
            if (!response.ok) {
                error(response.message ?: "No se pudieron cargar las metas.")
            }

            val goals = response.goals.map { goal ->
                MobileGoal(
                    id = goal.id,
                    goalType = goal.goalType,
                    goalTypeLabel = response.goalTypes[goal.goalType] ?: goal.goalType,
                    periodType = goal.periodType,
                    periodTypeLabel = response.periodTypes[goal.periodType] ?: goal.periodType,
                    targetValue = goal.targetValue,
                    currentValue = goal.currentValue,
                    progressPercent = goal.progressPercent,
                    platformId = goal.platformId,
                    platformName = goal.platformName,
                    languageId = goal.languageId,
                    languageName = goal.languageName,
                    periodStart = goal.periodStart,
                    periodEnd = goal.periodEnd,
                    autoRenew = goal.autoRenew
                )
            }
            goalDao.replaceAll(goals.map { it.toCacheEntity() })
            goals
        } catch (exception: Exception) {
            val cached = goalDao.all().map { it.toDomain() }
            if (cached.isNotEmpty()) cached else throw exception
        }
    }

    suspend fun create(
        goalType: String,
        periodType: String,
        targetValue: Int,
        platformId: Int,
        languageId: Int,
        autoRenew: Boolean
    ): Result<String> = runCatching {
        try {
            val response = api.storeGoal(
                MobileGoalCreateRequestDto(
                    goalType = goalType,
                    periodType = periodType,
                    targetValue = targetValue,
                    platformId = platformId,
                    languageId = languageId,
                    autoRenew = autoRenew
                )
            )
            if (!response.ok) {
                error(response.message ?: "No se pudo crear la meta.")
            }

            response.message ?: "Meta creada."
        } catch (exception: HttpException) {
            val apiMessage = exception.response()
                ?.errorBody()
                ?.string()
                ?.let { body -> errorAdapter.fromJson(body)?.message }

            error(apiMessage ?: "No se pudo crear la meta.")
        } catch (exception: IOException) {
            error("Disponible al iniciar sesión y tener conexión.")
        }
    }

    suspend fun update(
        id: Int,
        goalType: String,
        periodType: String,
        targetValue: Int,
        platformId: Int,
        languageId: Int,
        autoRenew: Boolean
    ): Result<String> = runCatching {
        try {
            val response = api.updateGoal(
                MobileGoalUpdateRequestDto(
                    id = id,
                    goalType = goalType,
                    periodType = periodType,
                    targetValue = targetValue,
                    platformId = platformId,
                    languageId = languageId,
                    autoRenew = autoRenew
                )
            )
            if (!response.ok) {
                error(response.message ?: "No se pudo actualizar la meta.")
            }

            response.message ?: "Meta actualizada."
        } catch (exception: HttpException) {
            val apiMessage = exception.response()
                ?.errorBody()
                ?.string()
                ?.let { body -> errorAdapter.fromJson(body)?.message }

            error(apiMessage ?: "No se pudo actualizar la meta.")
        } catch (exception: IOException) {
            error("Disponible al iniciar sesión y tener conexión.")
        }
    }
}

data class MobileGoalOptions(
    val goalTypes: List<MobileGoalOption>,
    val periodTypes: List<MobileGoalOption>,
    val platforms: List<MobilePlatform>,
    val languages: List<MobileLanguage>
)

data class MobileGoalOption(
    val value: String,
    val label: String
)
