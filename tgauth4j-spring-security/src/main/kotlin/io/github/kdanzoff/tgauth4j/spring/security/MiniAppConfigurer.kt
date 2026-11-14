package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.BotToken
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import org.springframework.context.ApplicationContext
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.time.Clock
import java.time.Duration

/**
 * Mini Apps authentication settings in [TelegramConfigurer.miniApp]. Each missing setting is taken from the Spring
 * context (bean of the same type), otherwise from a default:
 *
 * - verifier: [verifier], else a [TelegramInitDataVerifier] bean, else one built with [maxAge] and [clock]
 *   ([Clock] bean, else the system clock);
 * - bot token: [botTokenResolver] or [botToken], else a [BotTokenResolver] bean — **required**;
 * - `initData`: [initDataResolver], else the `Authorization: tma` header;
 * - users: [userDetailsService], else the unique [UserDetailsService] bean, else [DefaultTelegramUserDetailsService];
 * - after success: [authenticationSuccessHandler], else the request continues.
 */
public class MiniAppConfigurer internal constructor() {

    private var verifier: TelegramInitDataVerifier? = null
    private var maxAge: Duration? = null
    private var clock: Clock? = null
    private var botTokenResolver: BotTokenResolver? = null
    private var initDataResolver: InitDataResolver? = null
    private var userDetailsService: UserDetailsService? = null
    private var successHandler: AuthenticationSuccessHandler? = null

    /** Verifier; takes precedence over [maxAge] and [clock]. */
    public fun verifier(verifier: TelegramInitDataVerifier): MiniAppConfigurer = apply { this.verifier = verifier }

    /** Maximum age of `initData`. */
    public fun maxAge(maxAge: Duration): MiniAppConfigurer = apply { this.maxAge = maxAge }

    /** Clock of the verifier. */
    public fun clock(clock: Clock): MiniAppConfigurer = apply { this.clock = clock }

    /** Single bot, with token [token]. */
    public fun botToken(token: BotToken): MiniAppConfigurer = botTokenResolver(DefaultBotTokenResolver(token))

    /** Bot token of each request. */
    public fun botTokenResolver(resolver: BotTokenResolver): MiniAppConfigurer = apply { this.botTokenResolver = resolver }

    /** Reads `initData` from the request. */
    public fun initDataResolver(resolver: InitDataResolver): MiniAppConfigurer = apply { this.initDataResolver = resolver }

    /** Loads users: a [TelegramUserDetailsService] or a plain [UserDetailsService]. */
    public fun userDetailsService(service: UserDetailsService): MiniAppConfigurer = apply { this.userDetailsService = service }

    /** Called after a successful authentication; the request must continue (`chain.doFilter`). */
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
