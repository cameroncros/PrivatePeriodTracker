package com.cross.privateperiodtracker.lib

import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class ArgonHasher : KeyHasher {
    override fun keyFromPassword(password: String, salt: ByteArray): SecretKey {
        val argon2 = Argon2.Builder(Version.V13)
            .type(Type.Argon2id)
            .memoryCost(MemoryCost.MiB(32))
            .parallelism(1)
            .iterations(3)
            .build()

        val result = argon2.hash(password.toByteArray(), salt)
        return SecretKeySpec(result.hash, 0, result.hash.size, "AES")
    }
}