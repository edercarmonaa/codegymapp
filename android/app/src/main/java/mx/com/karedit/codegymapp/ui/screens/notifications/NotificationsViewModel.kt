package mx.com.karedit.codegymapp.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.NotificationsRepository
import mx.com.karedit.codegymapp.domain.model.MobileNotification

class NotificationsViewModel(private val repository: NotificationsRepository) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }
            repository.notifications()
                .onSuccess { data ->
                    _state.update {
                        it.copy(
                            unreadCount = data.unreadCount,
                            notifications = data.notifications
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudieron cargar las notificaciones.") }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun snackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

data class NotificationsUiState(
    val unreadCount: Int = 0,
    val notifications: List<MobileNotification> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)
