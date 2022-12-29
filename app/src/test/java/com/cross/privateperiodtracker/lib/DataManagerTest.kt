package com.cross.privateperiodtracker.lib

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.generateData
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


class DataManagerTest {

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

        listFiles(File(".")).forEach { file -> file.delete() }
    }

    @After
    fun tearDown() {
        listFiles(File(".")).forEach { file -> file.delete() }
    }

    @Test
    fun saveDataAndLoadDataTest() {
        val array = ByteArray(kotlin.math.abs(Random().nextInt() % 100) + 16)

        Random().nextBytes(array)
        val password = String(array, Charset.forName("UTF-8"))

        val data: PeriodData = generateData()


        val mockEncryptor = Encryptor(password, keyHasher = MockHasher())
        val dataManager = DataManager(context, mockEncryptor)
        dataManager.data = data
        dataManager.saveData()

        val data2: PeriodData? = dataManager.loadData()

        assertNotNull(data2)
        assertEquals(data, data2)
    }
}