package mx.com.karedit.codegymapp.data.local.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class DatabasePassphraseProvider(context: Context) {
    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        "codegym_secure_database",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getOrCreatePassphrase(): ByteArray {
        val stored = preferences.getString(KEY_PASSPHRASE, null)
        if (!stored.isNullOrBlank()) {
            return Base64.decode(stored, Base64.NO_WRAP)
        }

        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        preferences.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(bytes, Base64.NO_WRAP))
            .apply()
        return bytes
    }

    private companion object {
        const val KEY_PASSPHRASE = "room_sqlcipher_passphrase"
    }
}
