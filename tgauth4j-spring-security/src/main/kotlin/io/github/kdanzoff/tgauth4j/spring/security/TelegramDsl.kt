package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.BotToken
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import org.springframework.security.config.annotation.web.HttpSecurityDsl
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.time.Clock
import java.time.Duration

/** Scopes the DSL: `miniApp { }` does not see the settings of `telegram { }`. */
@DslMarker
public annotation class TelegramSecurityMarker

/**
 * Telegram authentication in the Spring Security Kotlin DSL, like `oauth2ResourceServer { }`:
 *
 * ```
 * http {
 *     telegram {
 *         miniApp {
 *             botToken = BotToken(token)
 *             maxAge = Duration.ofHours(1)
 *             userDetailsService = users
 *         }
 *         authenticationEntryPoint = entryPoint
 *     }
 * }
 * ```
 *
 * See [TelegramConfigurer] and [MiniAppConfigurer] for the defaults.
 */
public fun HttpSecurityDsl.telegram(configuration: TelegramDsl.() -> Unit) {
    val dsl = TelegramDsl().apply(configuration)
    this.with(TelegramConfigurer.telegram()) { dsl.applyTo(this) }
}

/** Settings of `telegram { }`. */
@TelegramSecurityMarker
public class TelegramDsl internal constructor() {

    /** 401 response; [TelegramAuthenticationEntryPoint] by default. */
    public var authenticationEntryPoint: AuthenticationEntryPoint? = null

    private var miniApp: MiniAppDsl.() -> Unit = {}

    /** Mini Apps settings. */
    public fun miniApp(configuration: MiniAppDsl.() -> Unit) {
        miniApp = configuration
    }

    internal fun applyTo(configurer: TelegramConfigurer) {
        authenticationEntryPoint?.let(configurer::authenticationEntryPoint)
        configurer.miniApp { MiniAppDsl().apply(miniApp).applyTo(it) }
    }
}

/** Settings of `miniApp { }`; see [MiniAppConfigurer]. */
@TelegramSecurityMarker
public class MiniAppDsl internal constructor() {

    /** Verifier; takes precedence over [maxAge] and [clock]. */
    public var verifier: TelegramInitDataVerifier? = null

    /** Maximum age of `initData`. */
    public var maxAge: Duration? = null

    /** Clock of the verifier. */
    public var clock: Clock? = null

    /** Single bot, with this token. */
    public var botToken: BotToken? = null

    /** Bot token of each request; takes precedence over [botToken]. */
    public var botTokenResolver: BotTokenResolver? = null

    /** Reads `initData` from the request. */
    public var initDataResolver: InitDataResolver? = null

    /** Loads users. */
    public var userDetailsService: UserDetailsService? = null

    /** Called after a successful authentication. */
    public var authenticationSuccessHandler: AuthenticationSuccessHandler? = null

    internal fun applyTo(configurer: MiniAppConfigurer) {
        verifier?.let(configurer::verifier)
        maxAge?.let(configurer::maxAge)
        clock?.let(configurer::clock)
        botToken?.let(configurer::botToken)
        botTokenResolver?.let(configurer::botTokenResolver)
        initDataResolver?.let(configurer::initDataResolver)
        userDetailsService?.let(configurer::userDetailsService)
        authenticationSuccessHandler?.let(configurer::authenticationSuccessHandler)
    }
}
