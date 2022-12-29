package com.cross.privateperiodtracker.lib

import java.io.Serializable
import javax.crypto.SecretKey

interface KeyHasher : Serializable {
    fun keyFromPassword(password: String, salt: ByteArray): SecretKey
}