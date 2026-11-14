package io.github.kdanzoff.tgauth4j.core

/**
 * Key used to verify an `initData`, specific to one bot.
 *
 * Only [BotToken] exists today (HMAC-SHA256 signature, with the bot token). A Telegram public key (Ed25519 signature,
 * without the token) can be added later without changing [TelegramInitDataVerifier.verify].
 */
public sealed interface VerificationKey {

    /** Id of the bot `initData` is signed for. */
    public val botId: Long
}
