package com.cross.privateperiodtracker.lib

import android.content.Context
import android.util.Base64
import java.io.Serializable
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

const val FORMAT = "AES/CBC/PKCS5Padding"

class FailedDecryption : Exception()

class Encryptor(password: String, context: Context, keyHasher: KeyHasher = ArgonHasher()) :
    Serializable {
    private val secretKey: SecretKey
    private var iv: ByteArray?
    private var salt: ByteArray

    init {
        val sp = context.getSharedPreferences("main", Context.MODE_PRIVATE)

        if (!sp.contains("salt")) {
            salt = ByteArray(16)
            Random().nextBytes(salt)
            sp.edit().putString("salt", String(Base64.encode(salt, Base64.DEFAULT))).apply()
        } else {
            val b64salt = sp.getString("salt", null)
            salt = Base64.decode(b64salt, Base64.DEFAULT)
        }

        if (!sp.contains("iv")) {
            iv = ByteArray(16)
            Random().nextBytes(iv)
            sp.edit().putString("iv", String(Base64.encode(iv, Base64.DEFAULT))).apply()
        } else {
            iv = Base64.decode(sp.getString("iv", null), Base64.DEFAULT)
        }

        secretKey = keyHasher.keyFromPassword(password, salt)
    }

    fun encrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(FORMAT)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(input)
    }

    fun decrypt(input: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance(FORMAT)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            return cipher.doFinal(input)
        } catch (e: Exception) {
            throw FailedDecryption()
        }
    }
}
