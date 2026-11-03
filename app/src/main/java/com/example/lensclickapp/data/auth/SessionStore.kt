package com.example.lensclickapp.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface SessionStore {
    fun readAccessToken(): String?

    fun writeAccessToken(accessToken: String)

    fun clear()
}

class AndroidKeystoreSessionStore(
    context: Context,
    private val keyAlias: String = "lensclick.mobile.session"
) : SessionStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "lensclick_secure_session",
        Context.MODE_PRIVATE
    )

    override fun readAccessToken(): String? {
        val encrypted = preferences.getString(CIPHERTEXT_KEY, null) ?: return null
        val iv = preferences.getString(IV_KEY, null) ?: throw storageFailure()
        return decrypt(encrypted, iv)
    }

    override fun writeAccessToken(accessToken: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val encrypted = cipher.doFinal(accessToken.toByteArray(Charsets.UTF_8))
            preferences.edit()
                .putString(CIPHERTEXT_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(IV_KEY, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        } catch (error: Exception) {
            throw storageFailure(error)
        }
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private fun decrypt(encrypted: String, iv: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(iv, Base64.NO_WRAP))
            )
            String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), Charsets.UTF_8)
        } catch (error: Exception) {
            throw storageFailure(error)
        }
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(keyAlias, null) as? SecretKey
        if (existing != null) return existing
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
        }.generateKey()
    }

    private fun storageFailure(cause: Throwable? = null) =
        MobileAuthException(MobileAuthErrorCode.STORAGE, cause = cause)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val CIPHERTEXT_KEY = "access_token"
        const val IV_KEY = "access_token_iv"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}

class InMemorySessionStore : SessionStore {
    private var accessToken: String? = null

    override fun readAccessToken(): String? = accessToken

    override fun writeAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    override fun clear() {
        accessToken = null
    }
}
