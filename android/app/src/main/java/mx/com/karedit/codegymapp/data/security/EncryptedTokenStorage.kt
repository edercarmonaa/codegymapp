package mx.com.karedit.codegymapp.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedTokenStorage(context: Context) : TokenStorage {
    private val preferences = createPreferencesWithSessionRecovery(context)

    private fun createPreferencesWithSessionRecovery(context: Context): SharedPreferences = try {
        createPreferences(context)
    } catch (firstError: Exception) {
        try {
            check(
                context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit()
            ) { "No se pudo limpiar la sesión cifrada dañada." }
            createPreferences(context)
        } catch (retryError: Exception) {
            retryError.addSuppressed(firstError)
            throw SecureStorageUnavailableException(
                "Android no pudo acceder a la sesión cifrada.",
                retryError
            )
        }
    }

    private fun createPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFERENCES_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun getToken(): String? = preferences.getString(KEY_TOKEN, null)

    override fun saveToken(token: String) {
        check(preferences.edit().putString(KEY_TOKEN, token).commit()) {
            "No se pudo guardar la sesión cifrada."
        }
    }

    override fun clearToken() {
        check(preferences.edit().remove(KEY_TOKEN).commit()) {
            "No se pudo eliminar la sesión cifrada."
        }
    }

    companion object {
        const val PREFERENCES_NAME = "codegym_secure_session"
        const val KEY_TOKEN = "jwt"
    }
}
