package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.BotToken
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import org.springframework.context.ApplicationContext
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.time.Clock
import java.time.Duration

/**
 * Réglages de l'authentification des Mini Apps dans [TelegramConfigurer.miniApp]. Chaque réglage absent est pris
 * dans le contexte Spring (bean du même type), sinon a une valeur par défaut :
 *
 * - vérificateur : [verifier], sinon bean [TelegramInitDataVerifier], sinon construit avec [maxAge] et [clock]
 *   (bean [Clock], sinon horloge système) ;
 * - jeton du bot : [botTokenResolver] ou [botToken], sinon bean [BotTokenResolver] — **obligatoire** ;
 * - `initData` : [initDataResolver], sinon en-tête `Authorization: tma` ;
 * - utilisateurs : [userDetailsService], sinon bean [UserDetailsService] unique, sinon [DefaultTelegramUserDetailsService] ;
 * - après succès : [authenticationSuccessHandler], sinon la requête continue.
 */
public class MiniAppConfigurer internal constructor() {

    private var verifier: TelegramInitDataVerifier? = null
    private var maxAge: Duration? = null
    private var clock: Clock? = null
    private var botTokenResolver: BotTokenResolver? = null
    private var initDataResolver: InitDataResolver? = null
    private var userDetailsService: UserDetailsService? = null
    private var successHandler: AuthenticationSuccessHandler? = null

    /** Vérificateur ; prioritaire sur [maxAge] et [clock]. */
    public fun verifier(verifier: TelegramInitDataVerifier): MiniAppConfigurer = apply { this.verifier = verifier }

    /** Âge maximal d'`initData`. */
    public fun maxAge(maxAge: Duration): MiniAppConfigurer = apply { this.maxAge = maxAge }

    /** Horloge du vérificateur. */
    public fun clock(clock: Clock): MiniAppConfigurer = apply { this.clock = clock }

    /** Un seul bot, de jeton [token]. */
    public fun botToken(token: BotToken): MiniAppConfigurer = botTokenResolver(DefaultBotTokenResolver(token))

    /** Jeton du bot de chaque requête. */
    public fun botTokenResolver(resolver: BotTokenResolver): MiniAppConfigurer = apply { this.botTokenResolver = resolver }

    /** Lecture d'`initData` dans la requête. */
    public fun initDataResolver(resolver: InitDataResolver): MiniAppConfigurer = apply { this.initDataResolver = resolver }

    /** Chargement des utilisateurs : [TelegramUserDetailsService] ou simple [UserDetailsService]. */
    public fun userDetailsService(service: UserDetailsService): MiniAppConfigurer = apply { this.userDetailsService = service }

    /** Suite d'une authentification réussie ; la requête doit continuer (`chain.doFilter`). */
    public fun authenticationSuccessHandler(handler: AuthenticationSuccessHandler): MiniAppConfigurer = apply { this.successHandler = handler }

    internal fun verifier(context: ApplicationContext?): TelegramInitDataVerifier =
        verifier ?: bean(context) ?: TelegramInitDataVerifier(maxAge ?: TelegramInitDataVerifier.DEFAULT_MAX_AGE, clock ?: bean(context) ?: Clock.systemUTC())

    internal fun botTokenResolver(context: ApplicationContext?): BotTokenResolver =
        botTokenResolver ?: bean(context) ?: error("no bot token: set miniApp botToken or botTokenResolver, or declare a BotTokenResolver bean")

    internal fun initDataResolver(context: ApplicationContext?): InitDataResolver = initDataResolver ?: bean(context) ?: HeaderInitDataResolver()

    internal fun userDetailsService(context: ApplicationContext?): UserDetailsService =
        userDetailsService ?: bean(context) ?: DefaultTelegramUserDetailsService()

    internal fun successHandler(): AuthenticationSuccessHandler = successHandler ?: ContinueRequestSuccessHandler

    private inline fun <reified T : Any> bean(context: ApplicationContext?): T? = context?.getBeanProvider(T::class.java)?.ifUnique
}
