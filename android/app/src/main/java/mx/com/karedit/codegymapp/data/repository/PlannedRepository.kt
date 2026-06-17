package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

class PlannedRepository(private val api: CodeGymApi) {
    suspend fun planned(): Result<List<MobileChallenge>> = runCatching {
        val response = api.mobilePlanned()
        if (!response.ok) {
            error(response.message ?: "No se pudo cargar Planeado.")
        }

        response.planned.map { it.toDomain() }
    }
}
