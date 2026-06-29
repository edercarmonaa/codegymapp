package mx.com.karedit.codegymapp.core.session

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import mx.com.karedit.codegymapp.ui.screens.auth.AuthViewModel

class AppLifecycleObserver(
    private val authViewModel: AuthViewModel,
    private val now: () -> Long = { System.currentTimeMillis() }
) : DefaultLifecycleObserver {
    private var backgroundedAt: Long = 0L

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAt = now()
    }

    override fun onStart(owner: LifecycleOwner) {
        val lastBackgroundedAt = backgroundedAt
        if (lastBackgroundedAt <= 0L) {
            return
        }

        authViewModel.onForegroundAfterBackground(now() - lastBackgroundedAt)
        backgroundedAt = 0L
    }
}
