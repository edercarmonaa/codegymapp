package mx.com.karedit.codegymapp.data.security

import android.content.Context
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import mx.com.karedit.codegymapp.data.local.CodeGymDatabase

object SecureLocalDataRecovery {
    fun reset(context: Context): Result<Unit> = runCatching {
        val applicationContext = context.applicationContext
        applicationContext.deleteDatabase(CodeGymDatabase.DATABASE_NAME)
        clearPreferences(applicationContext, DatabaseKeyProvider.PREFERENCES_NAME)
        clearPreferences(applicationContext, EncryptedTokenStorage.PREFERENCES_NAME)

        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        }
    }

    private fun clearPreferences(context: Context, name: String) {
        check(context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()) {
            "No se pudo limpiar el almacenamiento cifrado."
        }
    }

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
}
