package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeDto
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

class TodayRepository(private val api: CodeGymApi) {
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
}

data class TodayData(
    val today: List<MobileChallenge>,
    val expired: List<MobileChallenge>
)

private fun MobileChallengeDto.toDomain(): MobileChallenge =
    MobileChallenge(
        id = id,
        platformName = platformName,
        title = title.orEmpty(),
        scheduledDate = scheduledDate,
        completedDate = completedDate,
        status = status,
        difficulty = difficulty.orEmpty(),
        challengeUrl = challengeUrl,
        timeSpentMinutes = timeSpentMinutes,
        notes = notes.orEmpty(),
        origin = origin.orEmpty(),
        isRescheduled = isRescheduled
    )
