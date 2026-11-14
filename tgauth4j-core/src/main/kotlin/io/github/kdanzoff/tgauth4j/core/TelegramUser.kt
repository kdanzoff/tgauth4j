package io.github.kdanzoff.tgauth4j.core

/**
 * Telegram user, read from the `user` field of `initData`.
 *
 * Personal data: [toString] shows [id] only.
 *
 * @property id Telegram id, stable.
 * @property firstName first name.
 * @property lastName last name, if set.
 * @property username `@…` handle, optional and changeable: do not use it as an identifier.
 * @property languageCode language of the Telegram app (IETF tag), if known.
 * @property isPremium Telegram Premium subscriber.
 * @property photoUrl profile photo URL, if Telegram provides it.
 */
public data class TelegramUser(
    val id: Long,
    val firstName: String,
    val lastName: String? = null,
    val username: String? = null,
    val languageCode: String? = null,
    val isPremium: Boolean = false,
    val photoUrl: String? = null,
) {
    override fun toString(): String = "TelegramUser(id=$id)"
}
