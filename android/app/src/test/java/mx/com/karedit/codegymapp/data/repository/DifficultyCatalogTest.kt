package mx.com.karedit.codegymapp.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import mx.com.karedit.codegymapp.data.local.dao.CachedCatalogDao
import mx.com.karedit.codegymapp.data.local.entity.*
import mx.com.karedit.codegymapp.data.remote.dto.MobileCreateOptionsResponseDto
import mx.com.karedit.codegymapp.data.sync.*
import mx.com.karedit.codegymapp.domain.model.difficultyKey
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DifficultyCatalogTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test fun legacyDifficultyValuesMapToServerKeys() {
        listOf(" Fácil ", "FACIL", "Easy").forEach { assertEquals("facil", difficultyKey(it)) }
        listOf("Medio", "Medium", "intermedia").forEach { assertEquals("medio", difficultyKey(it)) }
        listOf("Difícil", "Hard").forEach { assertEquals("dificil", difficultyKey(it)) }
        assertEquals("legendario", difficultyKey("legendario"))
    }

    @Test fun remoteCatalogSurvivesStaticSeedingAndOlderBackend() = runBlocking {
        val dao = FakeCatalogDao()
        dao.seedDifficulties()
        val dto = moshi.adapter(MobileCreateOptionsResponseDto::class.java).fromJson(
            """{"ok":true,"difficulty_options":{"medio":"Intermedio","experto":"Experto"}}"""
        )!!
        dao.cacheDifficulties(dto.difficultyOptions)
        dao.seedDifficulties()
        dao.cacheDifficulties(null)
        assertEquals(listOf("medio", "experto"), dao.difficulties().map { it.value })
        assertEquals("Intermedio", dao.difficulties().first().label)
        assertNull(moshi.adapter(MobileCreateOptionsResponseDto::class.java).fromJson("""{"ok":true}""")!!.difficultyOptions)
    }

    @Test fun explicitEmptyCatalogClearsCachedChoices() = runBlocking {
        val dao = FakeCatalogDao()
        dao.seedDifficulties()
        dao.cacheDifficulties(emptyMap())
        assertTrue(dao.difficulties().isEmpty())
    }

    @Test fun pastAndInvalidRescheduleDatesAreRejected() {
        val today = LocalDate.of(2026, 11, 6)
        validateRescheduleDate("2026-11-06", today)
        validateRescheduleDate("2026-11-07", today)
        listOf("2026-11-05", "2026-02-30", "").forEach {
            assertTrue(runCatching { validateRescheduleDate(it, today) }.isFailure)
        }
    }

    @Test fun cancellationOnlyAllowsUnrescheduledOpenChallenges() {
        validateCancellation("pending", false)
        validateCancellation("expired", false)
        assertTrue(runCatching { validateCancellation("pending", true) }.isFailure)
        listOf("completed", "missed", "cancelled").forEach {
            assertTrue(runCatching { validateCancellation(it, false) }.isFailure)
        }
    }

    @Test fun reviewCorrectsDifficultyWithoutChangingOtherOfflineData() = runBlocking {
        val dao = FakePendingActionDao()
        val catalogs = FakeCatalogDao()
        catalogs.seedDifficulties()
        val payload = ManualChallengePayload(7, "Reto", "https://example.com", "unknown", 35, "Notas", listOf(1, 2), "https://github.com/example/repo")
        val adapter = moshi.adapter(ManualChallengePayload::class.java)
        dao.insert(PendingActionEntity(type = ActionTypes.CHALLENGE_MANUAL_CREATE, payloadJson = adapter.toJson(payload), createdAt = 1, errorKind = "validation"))
        val id = dao.pending().single().id
        val repository = PendingActionReviewRepository(dao, catalogs)
        assertTrue(repository.retry(id, "desconocido", "").isFailure)
        assertEquals(payload, adapter.fromJson(dao.pending().single().payloadJson))
        assertTrue(repository.retry(id, "Fácil", "").isSuccess)
        assertEquals(payload.copy(difficulty = "facil"), adapter.fromJson(dao.pending().single().payloadJson))
        assertEquals("", dao.pending().single().errorKind)
    }
}

private class FakeCatalogDao : CachedCatalogDao {
    private val rows = mutableListOf<CachedCatalogOptionEntity>()
    override suspend fun options(kind: String) = rows.filter { it.kind == kind }.sortedBy { it.sortOrder }
    override suspend fun insertOptions(options: List<CachedCatalogOptionEntity>) { rows.addAll(options) }
    override suspend fun clearOptions(kind: String) { rows.removeAll { it.kind == kind } }
    override suspend fun platforms() = emptyList<CachedPlatformEntity>()
    override suspend fun languages() = emptyList<CachedLanguageEntity>()
    override suspend fun insertPlatforms(platforms: List<CachedPlatformEntity>) = Unit
    override suspend fun insertLanguages(languages: List<CachedLanguageEntity>) = Unit
    override suspend fun clearPlatforms() = Unit
    override suspend fun clearLanguages() = Unit
}
