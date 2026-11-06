package mx.com.karedit.codegymapp.data.repository

import java.time.LocalDate
import mx.com.karedit.codegymapp.data.local.dao.CachedChallengeDao

internal fun validateRescheduleDate(value: String, today: LocalDate = LocalDate.now()) {
    val date = runCatching { LocalDate.parse(value) }.getOrNull()
    require(date != null && !date.isBefore(today)) {
        "Selecciona una fecha actual o futura para reprogramar."
    }
}

internal suspend fun CachedChallengeDao.validateCancellation(id: Int) {
    val challenge = getById(id).firstOrNull() ?: return
    validateCancellation(challenge.status, challenge.isRescheduled)
}

internal fun validateCancellation(status: String, isRescheduled: Boolean) {
    require(!isRescheduled && status in setOf("pending", "expired")) {
        "Este reto no se puede cancelar. Los retos reprogramados no admiten cancelación."
    }
}
