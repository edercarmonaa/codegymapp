package mx.com.karedit.codegymapp.domain.model

data class MobileNotification(
    val id: Int,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val actionUrl: String,
    val createdAt: String
)
