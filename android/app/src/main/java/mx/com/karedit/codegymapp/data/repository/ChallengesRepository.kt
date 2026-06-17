package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

class ChallengesRepository(private val api: CodeGymApi) {
    suspend fun challenges(month: String, status: String): Result<List<MobileChallenge>> = runCatching {
        val response = api.mobileChallenges(month = month, status = status)
        if (!response.ok) {
            error(response.message ?: "No se pudieron cargar los retos.")
        }

        response.challenges.map { it.toDomain() }
    }
}
