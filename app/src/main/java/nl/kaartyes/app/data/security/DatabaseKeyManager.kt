package nl.kaartyes.app.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseKeyManager @Inject constructor(
    private val context: Context
) {
    private companion object {
        const val PREFS_FILE = "kaartyes_secure_prefs"
        const val KEY_DB_PASSPHRASE = "db_passphrase"
        const val KEY_LENGTH_BYTES = 32
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getOrCreateDatabasePassphrase(): ByteArray {
        val stored = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)
        if (stored != null) {
            return Base64.decode(stored, Base64.NO_WRAP)
        }
        val newPassphrase = ByteArray(KEY_LENGTH_BYTES).also {
            SecureRandom().nextBytes(it)
        }
        encryptedPrefs.edit()
            .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(newPassphrase, Base64.NO_WRAP))
            .apply()
        return newPassphrase
    }
}
