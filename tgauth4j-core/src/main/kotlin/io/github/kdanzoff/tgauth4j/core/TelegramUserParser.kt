package io.github.kdanzoff.tgauth4j.core

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

/** Reads the JSON `user` field of `initData`. */
internal object TelegramUserParser {

    private val json = JsonMapper.builder().build()

    /** User described by [user], or `null` if it is malformed, has no positive id or no first name. */
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
