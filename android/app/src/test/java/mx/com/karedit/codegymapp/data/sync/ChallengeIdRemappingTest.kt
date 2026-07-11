package mx.com.karedit.codegymapp.data.sync

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChallengeIdRemappingTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun remapsTerminalChallengeAction() {
        val result = remapPendingChallengePayload(
            ActionTypes.CHALLENGE_COMPLETE,
            moshi.adapter(IdPayload::class.java).toJson(IdPayload(-10)),
            localId = -10,
            serverId = 81,
            moshi = moshi
        )

        assertEquals(81, moshi.adapter(IdPayload::class.java).fromJson(result!!)?.id)
    }

    @Test
    fun remapsRescheduleWithoutChangingDate() {
        val adapter = moshi.adapter(ReschedulePayload::class.java)
        val result = remapPendingChallengePayload(
            ActionTypes.CHALLENGE_RESCHEDULE,
            adapter.toJson(ReschedulePayload(-11, "2026-08-01")),
            localId = -11,
            serverId = 82,
            moshi = moshi
        )
        val payload = adapter.fromJson(result!!)

        assertEquals(82, payload?.id)
        assertEquals("2026-08-01", payload?.scheduledDate)
    }

    @Test
    fun remapsDetailsWithoutLosingContent() {
        val adapter = moshi.adapter(ChallengeDetailsPayload::class.java)
        val details = ChallengeDetailsPayload(
            id = -12,
            platformId = 3,
            title = "Reto seguro",
            challengeUrl = "https://example.com/challenge",
            difficulty = "medium",
            timeSpentMinutes = 45,
            notes = "Notas",
            languageIds = listOf(2),
            githubLinks = "https://github.com/example/repo"
        )
        val result = remapPendingChallengePayload(
            ActionTypes.CHALLENGE_SAVE_DETAILS,
            adapter.toJson(details),
            localId = -12,
            serverId = 83,
            moshi = moshi
        )
        val payload = adapter.fromJson(result!!)

        assertEquals(83, payload?.id)
        assertEquals(details.copy(id = 83), payload)
    }

    @Test
    fun ignoresPayloadForDifferentLocalId() {
        val result = remapPendingChallengePayload(
            ActionTypes.CHALLENGE_CANCEL,
            moshi.adapter(IdPayload::class.java).toJson(IdPayload(-20)),
            localId = -21,
            serverId = 90,
            moshi = moshi
        )

        assertNull(result)
    }
}
