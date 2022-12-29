package com.cross.privateperiodtracker.lib

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.io.File
import java.util.Random

class EncryptorTest {
    private lateinit var context: Context

    private fun generateAESBytes(): ByteArray {
        val bytes = ByteArray(16)
        Random().nextBytes(bytes)
        return bytes
    }

    @Before
    fun setUp() {
        val sharedPrefs: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        context = Mockito.mock(Context::class.java)
        Mockito.`when`(
            context.getSharedPreferences(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(sharedPrefs)
        Mockito.`when`(context.filesDir).thenReturn(File("."))
        Mockito.`when`(sharedPrefs.contains(ArgumentMatchers.anyString()))
            .thenReturn(true)

        val salt = generateAESBytes()
        val b64salt = String(Base64.encode(salt, Base64.DEFAULT))
        Mockito.`when`(sharedPrefs.getString("salt", null))
            .thenReturn(b64salt)

        val iv = generateAESBytes()
        val b64iv = String(Base64.encode(iv, Base64.DEFAULT))
        Mockito.`when`(sharedPrefs.getString("iv", null))
            .thenReturn(b64iv)

        listFiles(File(".")).forEach { file -> file.delete() }
    }

    @Test
    fun testEncryptionSuccess() {
        val encryptor = Encryptor("password", context, MockHasher())

        val bytes = ByteArray(1000)
        Random().nextBytes(bytes)

        val encryptedBytes = encryptor.encrypt(bytes)
        assertNotEquals(bytes, encryptedBytes)

        val decryptedBytes = encryptor.decrypt(encryptedBytes)
        assertEquals(bytes, decryptedBytes)
    }

    @Test
    fun testEncryptionWrongPassword() {
        val encryptor = Encryptor("password", context, MockHasher())
        val encryptor2 = Encryptor("password2", context, MockHasher())

        val bytes = ByteArray(1000)
        Random().nextBytes(bytes)

        val encryptedBytes = encryptor.encrypt(bytes)
        assertNotEquals(bytes, encryptedBytes)

        assertThrows(FailedDecryption::class.java) {
            encryptor2.decrypt(encryptedBytes)
        }
    }
}