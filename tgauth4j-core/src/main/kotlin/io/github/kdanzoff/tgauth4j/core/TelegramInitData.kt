package io.github.kdanzoff.tgauth4j.core

import java.time.Instant

/**
 * Verified `initData`: what Telegram signed when the Mini App was opened.
 *
 * Personal data: [toString] shows the user id and [authDate] only.
 *
 * @property user user who opened the Mini App.
 * @property authDate time Telegram signed the data.
 * @property queryId Mini App session id, for `answerWebAppQuery`, if present.
 * @property startParam `startapp` parameter of the opening link, if present.
 * @property chatType type of the chat the Mini App was opened from, if present.
 * @property chatInstance id of that chat, if present.
 * @property fields every received field, decoded (including those the library does not read).
 */
public data class TelegramInitData(
    val user: TelegramUser,
    val authDate: Instant,
    val queryId: String? = null,
    val startParam: String? = null,
    val chatType: String? = null,
    val chatInstance: String? = null,
    val fields: Map<String, String> = emptyMap(),
) {
    override fun toString(): String = "TelegramInitData(userId=${user.id}, authDate=$authDate)"
}
