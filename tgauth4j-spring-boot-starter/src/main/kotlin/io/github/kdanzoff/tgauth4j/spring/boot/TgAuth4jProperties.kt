package io.github.kdanzoff.tgauth4j.spring.boot

import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * `tgauth4j.*` properties.
 *
 * @property botToken bot token (`<id>:<secret>`); without it, no [io.github.kdanzoff.tgauth4j.spring.security.BotTokenResolver] is created.
 * @property initDataMaxAge maximum age of `initData`.
 */
@ConfigurationProperties("tgauth4j")
public class TgAuth4jProperties {
    public var botToken: String? = null
    public var initDataMaxAge: Duration = TelegramInitDataVerifier.DEFAULT_MAX_AGE

    override fun toString(): String = "TgAuth4jProperties(initDataMaxAge=$initDataMaxAge)"
}
