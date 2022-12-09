package com.cross.privateperiodtracker.lib

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.generateData
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.io.File
import java.nio.charset.Charset
import java.util.Random
import kotlin.math.abs


class EncryptionTest {

    private lateinit var context: Context;

    @Before
    fun setUp() {
        val sharedPrefs: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        context = Mockito.mock(Context::class.java)
        Mockito.`when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        Mockito.`when`(context.filesDir).thenReturn(File("."))
        Mockito.`when`(sharedPrefs.contains(anyString()))
            .thenReturn(true)

        val salt = generateAESBytes()
        val b64salt = String(Base64.encode(salt, Base64.DEFAULT))
        Mockito.`when`(sharedPrefs.getString("salt", null))
            .thenReturn(b64salt)

        val iv = generateAESBytes()
        val b64iv = String(Base64.encode(iv, Base64.DEFAULT))
        Mockito.`when`(sharedPrefs.getString("iv", null))
            .thenReturn(b64iv)

        listFiles(context).forEach { file -> file.delete() }
    }

    @After
    fun tearDown()
    {
        listFiles(context).forEach { file -> file.delete() }
    }

    @Test
    fun keyIsConsistent() {
        val salt = generateAESBytes()
        val key1 = keyFromPassword("abc123", salt)
        val key2 = keyFromPassword("abc123", salt)
        val key3 = keyFromPassword("notmatch", salt)

        assertEquals(key1, key2)
        assertThat(key1, not(key3))
    }

    @Test
    fun canaryMatches() {
        val array = ByteArray(abs(Random().nextInt() % 100))
        Random().nextBytes(array)
        val generatedString = String(array, Charset.forName("UTF-8"))
        val encryption = Encryption(generatedString, this.context)

        encryption.saveCanary()

        val files = listFiles(context)
        assertTrue(files.hasNext())

        val file = files.next()
        assertTrue(encryption.checkCanary(file))

        assertFalse(files.hasNext())
    }

    @Test
    fun canaryNotMatches() {
        listFiles(context).forEach { file -> file.delete() }

        val array = ByteArray(abs(Random().nextInt() % 100))

        Random().nextBytes(array)
        val generatedString = String(array, Charset.forName("UTF-8"))

        Random().nextBytes(array)
        val generatedString2 = String(array, Charset.forName("UTF-8"))

        assertThat(generatedString, not(generatedString2))

        val encryption = Encryption(generatedString, context)
        encryption.saveCanary()

        val files = listFiles(context)
        assertTrue(files.hasNext())

        val encryption2 = Encryption(generatedString2, context)
        assertFalse(encryption2.checkCanary(files.next()))

        assertFalse(files.hasNext())
    }

    @Test
    fun saveDataAndLoadDataTest()
    {
        val array = ByteArray(abs(Random().nextInt() % 100))

        Random().nextBytes(array)
        val generatedString = String(array, Charset.forName("UTF-8"))

        val data : PeriodData = generateData()

        val encryption = Encryption(generatedString, context)
        encryption.saveData(data)

        val data2 : PeriodData? = encryption.loadData();

        assertNotNull(data2)
        assertEquals(data, data2)
    }
}