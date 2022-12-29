package com.cross.privateperiodtracker.lib

import java.io.Serializable
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

const val FORMAT = "AES/GCM/NoPadding"
const val IV_LENGTH = 12
const val AAD_LENGTH = 16
const val TAG_LENGTH = 16
const val SALT_LENGTH = 16

class FailedDecryption : Exception()

class Encryptor(private val password: String, private val keyHasher: KeyHasher = ArgonHasher()) :
    Serializable {

    fun encrypt(input: ByteArray): ByteArray {
        val salt = SecureRandom().generateSeed(SALT_LENGTH)
        val iv = SecureRandom().generateSeed(IV_LENGTH)
        val key = keyHasher.keyFromPassword(password, salt)
        val cipher = Cipher.getInstance(FORMAT)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH * 8, iv))
        val aad = SecureRandom().generateSeed(AAD_LENGTH)
        cipher.updateAAD(aad)
        val ciphertext = cipher.doFinal(input)
        return salt + iv + aad + ciphertext
    }

    fun decrypt(input: ByteArray): ByteArray {
        try {
            val salt = input.sliceArray(0 until SALT_LENGTH)
            val iv = input.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH)
            val aad =
                input.sliceArray(SALT_LENGTH + IV_LENGTH until SALT_LENGTH + IV_LENGTH + AAD_LENGTH)
            val ciphertext = input.sliceArray(SALT_LENGTH + IV_LENGTH + AAD_LENGTH until input.size)

            val key = keyHasher.keyFromPassword(password, salt)

            val cipher = Cipher.getInstance(FORMAT)
            val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            cipher.updateAAD(aad)
            return cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw FailedDecryption()
        }
    }
}
