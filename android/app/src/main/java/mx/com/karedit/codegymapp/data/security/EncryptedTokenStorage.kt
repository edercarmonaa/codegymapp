package mx.com.karedit.codegymapp.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedTokenStorage(context: Context) : TokenStorage {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "codegym_secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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

    private companion object {
        const val KEY_TOKEN = "jwt"
    }
}
