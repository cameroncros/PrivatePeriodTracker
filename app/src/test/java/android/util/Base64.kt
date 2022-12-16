package android.util

import java.util.Base64


object Base64 {
    val DEFAULT: Int = 1

    @JvmStatic
    fun encodeToString(input: ByteArray?, @Suppress("UNUSED_PARAMETER") flags: Int): String {
        return Base64.getEncoder().encodeToString(input)
    }

    @JvmStatic
    fun encode(input: ByteArray?, @Suppress("UNUSED_PARAMETER") flags: Int): ByteArray {
        return Base64.getEncoder().encode(input)
    }

    @JvmStatic
    fun decode(str: String?, @Suppress("UNUSED_PARAMETER") flags: Int): ByteArray {
        return Base64.getDecoder().decode(str)
    }
}