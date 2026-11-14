package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.BotToken
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import org.springframework.security.config.annotation.web.HttpSecurityDsl
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.time.Clock
import java.time.Duration

/** Délimite le DSL : `miniApp { }` ne voit pas les réglages de `telegram { }`. */
@DslMarker
public annotation class TelegramSecurityMarker

/**
 * Authentification Telegram dans le DSL Kotlin de Spring Security, comme `oauth2ResourceServer { }` :
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
 * Voir [TelegramConfigurer] et [MiniAppConfigurer] pour les valeurs par défaut.
 */
public fun HttpSecurityDsl.telegram(configuration: TelegramDsl.() -> Unit) {
    val dsl = TelegramDsl().apply(configuration)
    this.with(TelegramConfigurer.telegram()) { dsl.applyTo(this) }
}

/** Réglages de `telegram { }`. */
@TelegramSecurityMarker
public class TelegramDsl internal constructor() {

    /** Réponse 401 ; [TelegramAuthenticationEntryPoint] par défaut. */
    public var authenticationEntryPoint: AuthenticationEntryPoint? = null

    private var miniApp: MiniAppDsl.() -> Unit = {}

    /** Réglages des Mini Apps. */
    public fun miniApp(configuration: MiniAppDsl.() -> Unit) {
        miniApp = configuration
    }

    internal fun applyTo(configurer: TelegramConfigurer) {
        authenticationEntryPoint?.let(configurer::authenticationEntryPoint)
        configurer.miniApp { MiniAppDsl().apply(miniApp).applyTo(it) }
    }
}

/** Réglages de `miniApp { }` ; voir [MiniAppConfigurer]. */
@TelegramSecurityMarker
public class MiniAppDsl internal constructor() {

    /** Vérificateur ; prioritaire sur [maxAge] et [clock]. */
    public var verifier: TelegramInitDataVerifier? = null

    /** Âge maximal d'`initData`. */
    public var maxAge: Duration? = null

    /** Horloge du vérificateur. */
    public var clock: Clock? = null

    /** Un seul bot, de ce jeton. */
    public var botToken: BotToken? = null

    /** Jeton du bot de chaque requête ; prioritaire sur [botToken]. */
    public var botTokenResolver: BotTokenResolver? = null

    /** Lecture d'`initData` dans la requête. */
    public var initDataResolver: InitDataResolver? = null

    /** Chargement des utilisateurs. */
    public var userDetailsService: UserDetailsService? = null

    /** Suite d'une authentification réussie. */
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
