package mx.com.karedit.codegymapp.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class DatabaseKeyProvider(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "codegym_secure_database",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun passphrase(): ByteArray {
        val existing = preferences.getString(KEY_DATABASE_PASSPHRASE, null)
        if (!existing.isNullOrBlank()) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }

        val bytes = ByteArray(PASSPHRASE_BYTES)
        SecureRandom().nextBytes(bytes)
        check(preferences.edit()
            .putString(KEY_DATABASE_PASSPHRASE, Base64.encodeToString(bytes, Base64.NO_WRAP))
            .commit()
        ) { "No se pudo proteger la clave de la base local." }
        return bytes
    }

    private companion object {
        const val KEY_DATABASE_PASSPHRASE = "db_passphrase"
        const val PASSPHRASE_BYTES = 32
    }
}
