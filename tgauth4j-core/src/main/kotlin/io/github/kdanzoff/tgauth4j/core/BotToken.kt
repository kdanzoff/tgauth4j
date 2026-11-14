package io.github.kdanzoff.tgauth4j.core

/**
 * Jeton d'un bot Telegram (`<identifiant du bot>:<secret>`), clé de la signature HMAC-SHA256 d'`initData`.
 *
 * Seule la clé dérivée est conservée, pas le jeton ; [toString] ne montre que [botId].
 *
 * @throws IllegalArgumentException si [value] n'a pas la forme d'un jeton de bot.
 */
public class BotToken(value: String) : VerificationKey {

    override val botId: Long = botIdOf(value)

    /** Clé secrète des Mini Apps : `HMAC-SHA256(clé = "WebAppData", message = jeton)`. */
    internal val webAppSecret: ByteArray = Hmac.sha256(WEB_APP_DATA, value.toByteArray(Charsets.UTF_8))

    override fun equals(other: Any?): Boolean = other is BotToken && other.botId == botId && other.webAppSecret.contentEquals(webAppSecret)

    override fun hashCode(): Int = botId.hashCode()

    override fun toString(): String = "BotToken(botId=$botId)"

    private companion object {
        /** Constante imposée par Telegram : la changer rendrait toute signature invalide. */
        val WEB_APP_DATA = "WebAppData".toByteArray(Charsets.UTF_8)

        fun botIdOf(value: String): Long {
            val separator = value.indexOf(':')
            val id = if (separator > 0 && separator < value.length - 1) value.substring(0, separator).toLongOrNull() else null
            require(id != null && id > 0) { "a bot token has the form <bot id>:<secret>" }
            return id
        }
    }
}
