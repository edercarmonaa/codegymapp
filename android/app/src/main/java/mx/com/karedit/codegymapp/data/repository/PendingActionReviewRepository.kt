package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.data.local.dao.CachedCatalogDao
import mx.com.karedit.codegymapp.data.local.dao.PendingActionDao
import mx.com.karedit.codegymapp.data.sync.*
import mx.com.karedit.codegymapp.domain.model.difficultyKey

class PendingActionReviewRepository(
    private val dao: PendingActionDao,
    private val catalogDao: CachedCatalogDao
) {
    val actions = dao.reviewActionsFlow()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun difficulties() = catalogDao.difficulties()

    // Decode and encode typed payloads so IDs and other fields keep their original types.
    suspend fun retry(id: Long, difficulty: String, date: String): Result<Unit> = runCatching {
        val action = dao.pending().firstOrNull { it.id == id } ?: error("La acción ya no está pendiente.")
        require(action.errorKind == "validation") { "Esta acción ya está lista para sincronizar." }
        val key = difficultyKey(difficulty)
        val json = when (action.type) {
            ActionTypes.CHALLENGE_CREATE -> {
                require(key.isBlank() || catalogDao.difficulties().any { it.value == key }) { "Selecciona una dificultad válida." }
                val adapter = moshi.adapter(ChallengeCreatePayload::class.java)
                adapter.toJson(requireNotNull(adapter.fromJson(action.payloadJson)).copy(difficulty = key))
            }
            ActionTypes.CHALLENGE_MANUAL_CREATE -> {
                require(catalogDao.difficulties().any { it.value == key }) { "Selecciona una dificultad válida." }
                val adapter = moshi.adapter(ManualChallengePayload::class.java)
                adapter.toJson(requireNotNull(adapter.fromJson(action.payloadJson)).copy(difficulty = key))
            }
            ActionTypes.CHALLENGE_SAVE_DETAILS -> {
                require(key.isBlank() || catalogDao.difficulties().any { it.value == key }) { "Selecciona una dificultad válida." }
                val adapter = moshi.adapter(ChallengeDetailsPayload::class.java)
                adapter.toJson(requireNotNull(adapter.fromJson(action.payloadJson)).copy(difficulty = key))
            }
            ActionTypes.CHALLENGE_RESCHEDULE -> {
                validateRescheduleDate(date)
                val adapter = moshi.adapter(ReschedulePayload::class.java)
                adapter.toJson(requireNotNull(adapter.fromJson(action.payloadJson)).copy(scheduledDate = date))
            }
            else -> action.payloadJson
        }
        dao.updatePayload(id, json)
    }
}
