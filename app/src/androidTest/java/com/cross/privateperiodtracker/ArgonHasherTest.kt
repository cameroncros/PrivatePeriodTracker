package com.cross.privateperiodtracker


import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cross.privateperiodtracker.lib.ArgonHasher
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Random

@RunWith(AndroidJUnit4::class)
class EncryptorTest {

    private fun generateAESBytes(): ByteArray {
        val bytes = ByteArray(16)
        Random().nextBytes(bytes)
        return bytes
    }

    @Test
    fun keyIsConsistent() {
        val encryptor = ArgonHasher()
        val salt = generateAESBytes()
        val key1 = encryptor.keyFromPassword("abc123", salt)
        val key2 = encryptor.keyFromPassword("abc123", salt)
        val key3 = encryptor.keyFromPassword("notmatch", salt)

        Assert.assertEquals(key1, key2)
        MatcherAssert.assertThat(key1, CoreMatchers.not(key3))
    }
}
