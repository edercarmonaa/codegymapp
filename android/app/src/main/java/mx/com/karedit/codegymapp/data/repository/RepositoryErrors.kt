package mx.com.karedit.codegymapp.data.repository

import java.io.IOException

internal const val OFFLINE_ACTION_MESSAGE = "Disponible al iniciar sesión y tener conexión."

internal fun Throwable.toOfflineReadException(sectionName: String): Throwable =
    if (this is IOException) {
        IllegalStateException("Sin conexión. Abre $sectionName con internet una vez para guardar datos offline.")
    } else {
        this
    }

internal fun Throwable.toOfflineSessionException(): Throwable =
    if (this is IOException) {
        IllegalStateException("Sin conexión. Se mostrará la información guardada disponible.")
    } else {
        this
    }
