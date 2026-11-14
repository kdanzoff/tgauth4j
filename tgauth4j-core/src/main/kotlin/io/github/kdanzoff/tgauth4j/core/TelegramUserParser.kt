package io.github.kdanzoff.tgauth4j.core

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

/** Lecture du champ JSON `user` d'`initData`. */
internal object TelegramUserParser {

    private val json = JsonMapper.builder().build()

    /** Utilisateur décrit par [user], ou `null` s'il est mal formé, sans identifiant positif ou sans prénom. */
    fun parse(user: String): TelegramUser? {
        val node = runCatching { json.readTree(user) }.getOrNull() ?: return null
        val id = node.get("id")?.takeIf { it.isIntegralNumber }?.longValue()?.takeIf { it > 0 } ?: return null
        val firstName = text(node, "first_name") ?: return null
        return TelegramUser(
            id = id,
            firstName = firstName,
            lastName = text(node, "last_name"),
            username = text(node, "username"),
            languageCode = text(node, "language_code"),
            isPremium = node.get("is_premium")?.takeIf { it.isBoolean }?.booleanValue() ?: false,
            photoUrl = text(node, "photo_url"),
        )
    }

    private fun text(node: JsonNode, name: String): String? = node.get(name)?.takeIf { it.isString }?.stringValue()
}
