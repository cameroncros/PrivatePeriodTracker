package com.cross.privateperiodtracker.lib

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class MockHasher : KeyHasher {
    override fun keyFromPassword(password: String, salt: ByteArray): SecretKey {
        return SecretKeySpec(password.toByteArray().sliceArray(0..16), 0, 16, "AES")
    }
}
