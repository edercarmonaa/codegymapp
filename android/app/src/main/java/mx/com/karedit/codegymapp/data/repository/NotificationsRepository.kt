package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileNotificationDto
import mx.com.karedit.codegymapp.domain.model.MobileNotification

class NotificationsRepository(private val api: CodeGymApi) {
    suspend fun notifications(): Result<NotificationsData> = runCatching {
        val response = api.mobileNotifications()
        if (!response.ok) {
            error(response.message ?: "No se pudieron cargar las notificaciones.")
        }

        NotificationsData(
            unreadCount = response.unreadCount,
            notifications = response.notifications.map { it.toDomain() }
        )
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
