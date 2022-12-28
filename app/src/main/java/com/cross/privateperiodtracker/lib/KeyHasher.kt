package com.cross.privateperiodtracker.lib

import javax.crypto.SecretKey

interface KeyHasher {
    fun keyFromPassword(password: String, salt: ByteArray): SecretKey
}