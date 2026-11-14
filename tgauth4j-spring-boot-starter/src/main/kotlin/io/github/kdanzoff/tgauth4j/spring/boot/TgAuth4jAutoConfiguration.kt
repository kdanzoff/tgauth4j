package io.github.kdanzoff.tgauth4j.spring.boot

import io.github.kdanzoff.tgauth4j.core.BotToken
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import io.github.kdanzoff.tgauth4j.spring.security.BotTokenResolver
import io.github.kdanzoff.tgauth4j.spring.security.DefaultBotTokenResolver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.time.Clock

/**
 * tgauth4j beans, used by `telegram { miniApp { } }` when the setting is not given: [TelegramInitDataVerifier]
 * (maximum age `tgauth4j.init-data-max-age`, [Clock] bean if any) and, if `tgauth4j.bot-token` is set,
 * [DefaultBotTokenResolver]. Each one backs off when the application defines its own bean.
 */
@AutoConfiguration
@EnableConfigurationProperties(TgAuth4jProperties::class)
public class TgAuth4jAutoConfiguration {

    /** `initData` verifier. */
    @Bean
    @ConditionalOnMissingBean
    public fun telegramInitDataVerifier(properties: TgAuth4jProperties, clock: ObjectProvider<Clock>): TelegramInitDataVerifier =
        TelegramInitDataVerifier(properties.initDataMaxAge, clock.getIfUnique { Clock.systemUTC() })

    /** Single bot, with token `tgauth4j.bot-token`. */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("tgauth4j.bot-token")
    public fun botTokenResolver(properties: TgAuth4jProperties): BotTokenResolver =
        DefaultBotTokenResolver(BotToken(requireNotNull(properties.botToken)))
}
