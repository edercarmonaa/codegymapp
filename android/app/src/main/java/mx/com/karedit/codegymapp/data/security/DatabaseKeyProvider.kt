package mx.com.karedit.codegymapp.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import mx.com.karedit.codegymapp.data.local.CodeGymDatabase

class DatabaseKeyProvider(private val context: Context) {

    fun passphrase(): ByteArray {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val preferences = EncryptedSharedPreferences.create(
                context,
                PREFERENCES_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val existing = preferences.getString(KEY_DATABASE_PASSPHRASE, null)
            if (!existing.isNullOrBlank()) {
                return Base64.decode(existing, Base64.NO_WRAP)
            }

            DatabaseKeyPolicy.requireSafeToCreate(
                databaseExists = context.getDatabasePath(CodeGymDatabase.DATABASE_NAME).exists()
            )

            val bytes = ByteArray(PASSPHRASE_BYTES)
            SecureRandom().nextBytes(bytes)
            if (!preferences.edit()
                    .putString(KEY_DATABASE_PASSPHRASE, Base64.encodeToString(bytes, Base64.NO_WRAP))
                    .commit()
            ) {
                throw SecureStorageUnavailableException("No se pudo proteger la clave de la base local.")
            }
            return bytes
        } catch (error: SecureStorageUnavailableException) {
            throw error
        } catch (error: Exception) {
            throw SecureStorageUnavailableException(
                "Android no pudo acceder a la clave que protege los datos locales.",
                error
            )
        }
    }

    companion object {
        const val PREFERENCES_NAME = "codegym_secure_database"
        const val KEY_DATABASE_PASSPHRASE = "db_passphrase"
        const val PASSPHRASE_BYTES = 32
    }
}

internal object DatabaseKeyPolicy {
    fun requireSafeToCreate(databaseExists: Boolean) {
        if (databaseExists) {
            throw SecureStorageUnavailableException(
                "La clave de la base local no está disponible."
            )
        }
    }
}
