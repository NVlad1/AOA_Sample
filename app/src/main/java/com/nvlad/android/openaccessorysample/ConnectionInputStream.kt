package com.nvlad.android.openaccessorysample

import java.io.IOException

interface ConnectionInputStream {
    @Throws(IOException::class)
    fun read(rxBuffer: ByteArray?): Int

    @Throws(IOException::class)
    fun close()
}