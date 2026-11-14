package io.github.kdanzoff.tgauth4j.core

/**
 * Clé de vérification d'un `initData`, propre à un bot.
 *
 * Seul [BotToken] existe aujourd'hui (signature HMAC-SHA256, avec le jeton du bot). Une clé publique de Telegram
 * (signature Ed25519, sans le jeton) pourra s'ajouter sans changer [TelegramInitDataVerifier.verify].
 */
public sealed interface VerificationKey {

    /** Identifiant du bot pour lequel `initData` est signé. */
    public val botId: Long
}
