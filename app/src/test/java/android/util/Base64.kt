package android.util

import java.util.Base64


public object Base64 {
    val DEFAULT: Int = 1

    @JvmStatic
    public fun encodeToString(input: ByteArray?, @Suppress("UNUSED_PARAMETER") flags: Int): String {
        return Base64.getEncoder().encodeToString(input)
    }

    @JvmStatic
    public fun encode(input: ByteArray?, @Suppress("UNUSED_PARAMETER") flags: Int): ByteArray {
        return Base64.getEncoder().encode(input)
    }

    @JvmStatic
    public fun decode(str: String?, @Suppress("UNUSED_PARAMETER") flags: Int): ByteArray {
        return Base64.getDecoder().decode(str)
    }
}