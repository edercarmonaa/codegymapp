package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.local.dao.CachedNotificationDao
import mx.com.karedit.codegymapp.data.local.mapper.toCacheEntity
import mx.com.karedit.codegymapp.data.local.mapper.toDomain
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileNotificationActionRequestDto
import mx.com.karedit.codegymapp.data.remote.dto.MobileNotificationDto
import mx.com.karedit.codegymapp.data.sync.ActionTypes
import mx.com.karedit.codegymapp.data.sync.OfflineActionQueue
import mx.com.karedit.codegymapp.domain.model.MobileNotification

class NotificationsRepository(
    private val api: CodeGymApi,
    private val notificationDao: CachedNotificationDao,
    private val offlineActionQueue: OfflineActionQueue
) {
    suspend fun notifications(): Result<NotificationsData> = runCatching {
        try {
            val response = api.mobileNotifications()
            if (!response.ok) {
                error(response.message ?: "No se pudieron cargar las notificaciones.")
            }

            val notifications = response.notifications.map { it.toDomain() }
            notificationDao.replaceAll(notifications.map { it.toCacheEntity() })
            NotificationsData(
                unreadCount = response.unreadCount,
                notifications = notifications
            )
        } catch (exception: Exception) {
            val cached = notificationDao.all().map { it.toDomain() }
            if (cached.isNotEmpty()) {
                NotificationsData(
                    unreadCount = notificationDao.unreadCount(),
                    notifications = cached
                )
            } else {
                throw exception.toOfflineReadException("Notificaciones")
            }
        }
    }

    suspend fun markRead(id: Int): Result<String> =
        notificationAction(
            fallbackMessage = "No se pudo marcar la notificación.",
            offline = {
                offlineActionQueue.enqueueNotificationAction(ActionTypes.NOTIFICATION_MARK_READ, id)
                notificationDao.markRead(id)
            }
        ) {
            api.markNotificationRead(MobileNotificationActionRequestDto(id))
        }

    suspend fun delete(id: Int): Result<String> =
        notificationAction(
            fallbackMessage = "No se pudo eliminar la notificación.",
            offline = {
                offlineActionQueue.enqueueNotificationAction(ActionTypes.NOTIFICATION_DELETE, id)
                notificationDao.delete(id)
            }
        ) {
            api.deleteNotification(MobileNotificationActionRequestDto(id))
        }

    private suspend fun notificationAction(
        fallbackMessage: String,
        offline: suspend () -> Unit,
        action: suspend () -> mx.com.karedit.codegymapp.data.remote.dto.MobileActionResponseDto
    ): Result<String> =
        runCatching {
            try {
                val response = action()
                if (!response.ok) {
                    error(response.message ?: fallbackMessage)
                }

                response.message ?: "Notificación actualizada."
            } catch (exception: java.io.IOException) {
                offline()
                "Acción guardada para sincronizar."
            }
        }
}

data class NotificationsData(
    val unreadCount: Int,
    val notifications: List<MobileNotification>
)

private fun MobileNotificationDto.toDomain(): MobileNotification =
    MobileNotification(
        id = id,
        type = type,
        title = title,
        message = message,
        isRead = isRead,
        actionUrl = actionUrl,
        createdAt = createdAt
    )
