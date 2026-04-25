package com.carlos.autoflow.workflow.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object AfwCodec {
    private const val afwPrefix = "AFW:"

    fun isAfw(text: String): Boolean = text.trim().startsWith(afwPrefix)

    fun encodeJsonToAfw(json: String): String {
        val gzipped = gzip(json.trim().toByteArray(Charsets.UTF_8))
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString(gzipped)
        return afwPrefix + payload
    }

    fun decodeAfwToJson(afw: String): String {
        val payload = afw.trim().removePrefix(afwPrefix)
        require(payload.isNotBlank()) { "AFW 内容为空" }
        val decoded = Base64.getUrlDecoder().decode(payload)
        return ungzip(decoded).toString(Charsets.UTF_8)
    }

    fun normalizeToJson(text: String): String {
        val trimmed = text.trim()
        return if (isAfw(trimmed)) decodeAfwToJson(trimmed) else trimmed
    }

    private fun gzip(input: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(input) }
        return output.toByteArray()
    }

    private fun ungzip(input: ByteArray): ByteArray {
        return GZIPInputStream(ByteArrayInputStream(input)).use { it.readBytes() }
    }
}
