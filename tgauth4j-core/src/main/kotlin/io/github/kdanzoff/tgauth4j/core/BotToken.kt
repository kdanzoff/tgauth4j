package io.github.kdanzoff.tgauth4j.core

/**
 * Telegram bot token (`<bot id>:<secret>`), the key of the HMAC-SHA256 signature of `initData`.
 *
 * Only the derived key is kept, not the token; [toString] shows [botId] only.
 *
 * @throws IllegalArgumentException if [value] does not have the form of a bot token.
 */
public class BotToken(value: String) : VerificationKey {

    override val botId: Long = botIdOf(value)

    /** Mini Apps secret key: `HMAC-SHA256(key = "WebAppData", message = token)`. */
    internal val webAppSecret: ByteArray = Hmac.sha256(WEB_APP_DATA, value.toByteArray(Charsets.UTF_8))

    override fun equals(other: Any?): Boolean = other is BotToken && other.botId == botId && other.webAppSecret.contentEquals(webAppSecret)

    override fun hashCode(): Int = botId.hashCode()

    override fun toString(): String = "BotToken(botId=$botId)"

    private companion object {
        /** Constant defined by Telegram: changing it would make every signature invalid. */
        val WEB_APP_DATA = "WebAppData".toByteArray(Charsets.UTF_8)

        fun botIdOf(value: String): Long {
            val separator = value.indexOf(':')
            val id = if (separator > 0 && separator < value.length - 1) value.substring(0, separator).toLongOrNull() else null
            require(id != null && id > 0) { "a bot token has the form <bot id>:<secret>" }
            return id
        }
    }
}
