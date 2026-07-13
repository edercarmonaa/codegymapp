package mx.com.karedit.codegymapp.data.sync

import java.io.IOException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SyncFailurePolicyTest {
    @Test
    fun `red es temporal y detiene la cola`() {
        val failure = classifySyncFailure(IOException("offline"))

        assertEquals(SyncErrorKinds.NETWORK, failure.kind)
        assertTrue(failure.retryable)
        assertTrue(failure.stopsQueue)
    }

    @Test
    fun `validacion permanente permite continuar con otras acciones`() {
        val failure = classifySyncFailure(httpException(422))

        assertEquals(SyncErrorKinds.VALIDATION, failure.kind)
        assertFalse(failure.retryable)
        assertFalse(failure.stopsQueue)
    }

    @Test
    fun `sesion expirada detiene la cola sin reintento automatico`() {
        val failure = classifySyncFailure(httpException(401))

        assertEquals(SyncErrorKinds.AUTH, failure.kind)
        assertFalse(failure.retryable)
        assertTrue(failure.stopsQueue)
    }

    @Test
    fun `backoff crece y queda limitado a seis horas`() {
        assertEquals(60_000L, retryDelayMillis(1))
        assertEquals(120_000L, retryDelayMillis(2))
        assertEquals(6 * 60 * 60 * 1_000L, retryDelayMillis(20))
    }

    private fun httpException(code: Int): HttpException = HttpException(
        Response.error<Unit>(code, "error".toResponseBody())
    )
}
