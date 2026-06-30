package mx.com.karedit.codegymapp.data.repository

import java.io.IOException

internal const val OFFLINE_ACTION_MESSAGE = "Sin conexión a internet."
private const val OFFLINE_READ_MESSAGE = "Sin conexión a internet."

internal fun Throwable.toOfflineReadException(sectionName: String): Throwable =
    if (this is IOException) {
        IllegalStateException(OFFLINE_READ_MESSAGE)
    } else {
        this
    }

internal fun Throwable.toOfflineSessionException(): Throwable =
    if (this is IOException) {
        IllegalStateException(OFFLINE_READ_MESSAGE)
    } else {
        this
    }
