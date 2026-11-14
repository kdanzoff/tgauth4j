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
 * Beans de tgauth4j, repris par `telegram { miniApp { } }` quand le réglage n'est pas donné :
 * [TelegramInitDataVerifier] (âge maximal `tgauth4j.init-data-max-age`, bean [Clock] s'il existe) et, si
 * `tgauth4j.bot-token` est renseigné, [DefaultBotTokenResolver]. Chacun cède la place à un bean de l'application.
 */
@AutoConfiguration
@EnableConfigurationProperties(TgAuth4jProperties::class)
public class TgAuth4jAutoConfiguration {

    /** Vérificateur d'`initData`. */
    @Bean
    @ConditionalOnMissingBean
    public fun telegramInitDataVerifier(properties: TgAuth4jProperties, clock: ObjectProvider<Clock>): TelegramInitDataVerifier =
        TelegramInitDataVerifier(properties.initDataMaxAge, clock.getIfUnique { Clock.systemUTC() })

    /** Un seul bot, de jeton `tgauth4j.bot-token`. */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("tgauth4j.bot-token")
    public fun botTokenResolver(properties: TgAuth4jProperties): BotTokenResolver =
        DefaultBotTokenResolver(BotToken(requireNotNull(properties.botToken)))
}
