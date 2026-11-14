package io.github.kdanzoff.tgauth4j.core

import java.net.URLDecoder

/** Reads the fields of `initData` (URL-encoded `key=value` pairs separated by `&`). */
internal object InitDataFields {

    /** Decoded fields, or `null` if [initData] is blank or malformed. */
    fun parse(initData: String): Map<String, String>? {
        val pairs = initData.split('&')
        if (initData.isBlank() || pairs.any { it.indexOf('=') <= 0 }) return null
        return runCatching { pairs.associate(::decode) }.getOrNull()
    }

    /** Signed string: fields sorted by key, `key=value` separated by a line feed, without [excluded]. */
    fun dataCheckString(fields: Map<String, String>, excluded: String): String =
        fields.filterKeys { it != excluded }.toSortedMap().entries.joinToString("\n") { "${it.key}=${it.value}" }

    private fun decode(pair: String): Pair<String, String> {
        val separator = pair.indexOf('=')
        return URLDecoder.decode(pair.substring(0, separator), Charsets.UTF_8) to URLDecoder.decode(pair.substring(separator + 1), Charsets.UTF_8)
    }
}
