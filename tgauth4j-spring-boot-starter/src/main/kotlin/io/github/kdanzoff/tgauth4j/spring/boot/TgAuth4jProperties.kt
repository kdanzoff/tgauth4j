package io.github.kdanzoff.tgauth4j.spring.boot

import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Propriétés `tgauth4j.*`.
 *
 * @property botToken jeton du bot (`<identifiant>:<secret>`) ; sans lui, aucun [io.github.kdanzoff.tgauth4j.spring.security.BotTokenResolver] n'est créé.
 * @property initDataMaxAge âge maximal d'`initData`.
 */
@ConfigurationProperties("tgauth4j")
public class TgAuth4jProperties {
    public var botToken: String? = null
    public var initDataMaxAge: Duration = TelegramInitDataVerifier.DEFAULT_MAX_AGE

    override fun toString(): String = "TgAuth4jProperties(initDataMaxAge=$initDataMaxAge)"
}
