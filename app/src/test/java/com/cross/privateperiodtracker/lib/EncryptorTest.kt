package com.cross.privateperiodtracker.lib

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Random

class EncryptorTest {
    @Before
    fun setUp() {
        listFiles(File(".")).forEach { file -> file.delete() }
    }

    @Test
    fun testEncryptionSuccess() {
        val encryptor = Encryptor("password", MockHasher())

        val bytes = ByteArray(1000)
        Random().nextBytes(bytes)

        val encryptedBytes = encryptor.encrypt(bytes)
        assertThat(bytes, not(equalTo(encryptedBytes)))

        val decryptedBytes = encryptor.decrypt(encryptedBytes)
        assertThat(bytes, equalTo(decryptedBytes))
    }

    @Test
    fun testEncryptionOutputsAreDifferent() {
        val encryptor = Encryptor("password", MockHasher())

        val bytes = ByteArray(1000)
        Random().nextBytes(bytes)

        val encryptedBytes = encryptor.encrypt(bytes)
        val encryptedBytes2 = encryptor.encrypt(bytes)
        assertThat(bytes, not(equalTo(encryptedBytes)))
        assertThat(encryptedBytes, not(equalTo(encryptedBytes2)))
    }

    @Test
    fun testEncryptionWrongPassword() {
        val encryptor = Encryptor("password", MockHasher())
        val encryptor2 = Encryptor("password2", MockHasher())

        val bytes = ByteArray(1000)
        Random().nextBytes(bytes)

        val encryptedBytes = encryptor.encrypt(bytes)
        assertThat(bytes, not(equalTo(encryptedBytes)))

        assertThrows(FailedDecryption::class.java) {
            encryptor2.decrypt(encryptedBytes)
        }
    }
}