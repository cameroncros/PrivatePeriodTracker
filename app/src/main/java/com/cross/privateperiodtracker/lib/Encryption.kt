package com.cross.privateperiodtracker.lib

import android.R.attr
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Base64
import com.cross.privateperiodtracker.data.PeriodData
import com.cross.privateperiodtracker.data.PeriodEvent
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Serializable
import java.security.spec.KeySpec
import java.time.LocalDateTime
import java.util.Random
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


fun generateAESBytes(): ByteArray {
    val bytes = ByteArray(16)
    Random().nextBytes(bytes)
    return bytes
}

fun keyFromPassword(password: String, salt: ByteArray): SecretKey {
    val argon2 = Argon2.Builder(Version.V13)
        .type(Type.Argon2id)
        .memoryCost(MemoryCost.MiB(32))
        .parallelism(1)
        .iterations(3)
        .build()

    val result = argon2.hash(password.toByteArray(), salt)
    return SecretKeySpec(result.hash, 0, result.hash.size, "AES")
}

fun generateFilename(filesDir: File): File {
    return File(
        filesDir.canonicalPath + File.separator + "file_" + UUID.randomUUID().toString()
    )
}

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

class Encryption(password: String, context: Context) : Serializable {
    private val secretKey: SecretKey
    private var iv: ByteArray?
    private var file: File? = null
    var data: PeriodData = PeriodData()
    private var filesDir: File

    init {
        val sp = context.getSharedPreferences("main", MODE_PRIVATE)

        val salt: ByteArray
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

        filesDir = context.filesDir
        secretKey = keyFromPassword(password, salt)
    }

    fun saveData() {
        val moshi: Moshi =
            Moshi.Builder().add(PeriodEventArrayListMoshiAdapter()).add(LocalDateTimeMoshiAdapter())
                .build()
        val jsonAdapter: JsonAdapter<PeriodData> = moshi.adapter(PeriodData::class.java)

        val json: String = jsonAdapter.toJson(data)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val cipherBytes = cipher.doFinal(json.toByteArray())

        if (file == null) {
            file = generateFilename(filesDir)
        }
        val outputStream = FileOutputStream(file)
        outputStream.write(cipherBytes)
        outputStream.close()
    }

    fun loadData(): PeriodData? {
        for (file in listFiles(filesDir)) {
            val pd = decryptFile(file)
            if (pd != null) {
                this.file = file
                this.data = pd
                return pd
            }
        }
        return null
    }

    private fun decryptFile(file: File): PeriodData? {
        try {
            val inputStream = FileInputStream(file)
            val encryptedData = inputStream.readBytes()
            inputStream.close()

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val decryptedBytes = String(cipher.doFinal(encryptedData))

            val moshi: Moshi =
                Moshi.Builder().add(PeriodEventArrayListMoshiAdapter())
                    .add(LocalDateTimeMoshiAdapter())
                    .build()
            val jsonAdapter: JsonAdapter<PeriodData> = moshi.adapter(PeriodData::class.java)

            return jsonAdapter.fromJson(decryptedBytes)
        } catch (bpe: Exception) {
            return null
        }
    }
}