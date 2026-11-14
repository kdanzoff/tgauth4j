package io.github.kdanzoff.tgauth4j.core

import java.net.URLDecoder

/** Lecture des champs d'`initData` (`clé=valeur` encodés en URL, séparés par `&`). */
internal object InitDataFields {

    /** Champs décodés, ou `null` si [initData] est vide ou mal formé. */
    fun parse(initData: String): Map<String, String>? {
        val pairs = initData.split('&')
        if (initData.isBlank() || pairs.any { it.indexOf('=') <= 0 }) return null
        return runCatching { pairs.associate(::decode) }.getOrNull()
    }

    /** Chaîne signée : champs triés par clé, `clé=valeur` séparés par un saut de ligne, sans [excluded]. */
    fun dataCheckString(fields: Map<String, String>, excluded: String): String =
        fields.filterKeys { it != excluded }.toSortedMap().entries.joinToString("\n") { "${it.key}=${it.value}" }

    private fun decode(pair: String): Pair<String, String> {
        val separator = pair.indexOf('=')
        return URLDecoder.decode(pair.substring(0, separator), Charsets.UTF_8) to URLDecoder.decode(pair.substring(separator + 1), Charsets.UTF_8)
    }
}
