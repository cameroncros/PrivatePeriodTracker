package com.cross.privateperiodtracker.lib

import android.content.Context
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.PeriodEvent
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

fun listFiles(filesDir: File) = iterator {
    val dir = filesDir.listFiles() ?: return@iterator
    for (file in dir) {
        if (file.isDirectory) {
            continue
        }
        if (!file.name.startsWith("file_")) {
            continue
        }
        yield(file)
    }
}

class PeriodEventArrayListMoshiAdapter {
    @ToJson
    fun arrayListToJson(list: ArrayList<PeriodEvent>): List<PeriodEvent> = list

    @FromJson
    fun arrayListFromJson(list: List<PeriodEvent>): ArrayList<PeriodEvent> = ArrayList(list)
}

class LocalDateTimeMoshiAdapter {
    @ToJson
    fun localDateTimeToJson(ldt: LocalDateTime): String = ldt.toString()

    @FromJson
    fun localDateTimeFromJson(string: String): LocalDateTime = LocalDateTime.parse(string)
}

class DataManager(context: Context, private val encryptor: Encryptor) : Serializable {
    private var file: File? = null
    var data: PeriodData = PeriodData()
    var filesDir: File

    init {
        filesDir = context.filesDir
    }

    private fun generateFilename(filesDir: File): File {
        return File(
            filesDir.canonicalPath + File.separator + "file_" + UUID.randomUUID().toString()
        )
    }

    fun saveData() {
        val moshi: Moshi =
            Moshi.Builder().add(PeriodEventArrayListMoshiAdapter()).add(LocalDateTimeMoshiAdapter())
                .build()
        val jsonAdapter: JsonAdapter<PeriodData> = moshi.adapter(PeriodData::class.java)

        val json: String = jsonAdapter.toJson(data)

        val encryptedBytes = encryptor.encrypt(json.toByteArray())

        if (file == null) {
            file = generateFilename(filesDir)
        }
        val outputStream = FileOutputStream(file)
        outputStream.write(encryptedBytes)
        outputStream.close()
    }

    fun loadData(): PeriodData? {
        for (file in listFiles(filesDir)) {
            val pd = loadFile(file)
            if (pd != null) {
                this.file = file
                this.data = pd
                return pd
            }
        }
        return null
    }

    private fun loadFile(file: File): PeriodData? {
        try {
            val inputStream = FileInputStream(file)
            val encryptedData = inputStream.readBytes()
            inputStream.close()

            val decryptedBytes = encryptor.decrypt(encryptedData)

            val moshi: Moshi =
                Moshi.Builder().add(PeriodEventArrayListMoshiAdapter())
                    .add(LocalDateTimeMoshiAdapter())
                    .build()
            val jsonAdapter: JsonAdapter<PeriodData> = moshi.adapter(PeriodData::class.java)

            return jsonAdapter.fromJson(String(decryptedBytes))
        } catch (bpe: Exception) {
            return null
        }
    }
}