package mx.com.karedit.codegymapp.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import mx.com.karedit.codegymapp.di.AppContainer

class CodeGymFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AppContainer(applicationContext).fcmTokenRegistrar.registerToken(token)
    }
}
