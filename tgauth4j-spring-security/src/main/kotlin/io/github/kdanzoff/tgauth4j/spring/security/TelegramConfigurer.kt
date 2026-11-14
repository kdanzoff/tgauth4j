package io.github.kdanzoff.tgauth4j.spring.security

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler
import org.springframework.security.web.authentication.AuthenticationFilter
import org.springframework.security.web.util.matcher.AnyRequestMatcher

/**
 * Spring Security configurer for Telegram authentication, modelled on `OAuth2ResourceServerConfigurer`:
 *
 * ```java
 * http.with(TelegramConfigurer.telegram(), telegram -> telegram
 *     .miniApp(miniApp -> miniApp.botToken(new BotToken(token)))
 *     .authenticationEntryPoint(entryPoint));
 * ```
 *
 * In Kotlin: `http { telegram { miniApp { … } } }` ([telegram]).
 *
 * - `init`: [TelegramAuthenticationEntryPoint] (or [authenticationEntryPoint]) becomes the **default** entry point of
 *   the chain; an entry point declared in `exceptionHandling` still takes precedence;
 * - `configure`: an `AuthenticationFilter` (reading by [TelegramAuthenticationConverter], verification by
 *   [TelegramAuthenticationProvider]) before request authorization, without redirect or session; a rejected
 *   `initData` goes through the same entry point.
 *
 * Only Mini Apps are supported for now ([miniApp]).
 */
public class TelegramConfigurer : AbstractHttpConfigurer<TelegramConfigurer, HttpSecurity>() {

    private val miniApp = MiniAppConfigurer()
    private var entryPoint: AuthenticationEntryPoint = TelegramAuthenticationEntryPoint()

    /** Mini Apps settings ([MiniAppConfigurer]). */
    public fun miniApp(customizer: Customizer<MiniAppConfigurer>): TelegramConfigurer = apply { customizer.customize(miniApp) }

    /** Response to an unauthenticated request or a rejected `initData` (401). */
    public fun authenticationEntryPoint(entryPoint: AuthenticationEntryPoint): TelegramConfigurer = apply { this.entryPoint = entryPoint }

    override fun init(http: HttpSecurity) {
        @Suppress("UNCHECKED_CAST")
        val type = ExceptionHandlingConfigurer::class.java as Class<ExceptionHandlingConfigurer<HttpSecurity>>
        http.getConfigurer(type)?.defaultAuthenticationEntryPointFor(entryPoint, AnyRequestMatcher.INSTANCE)
    }

    override fun configure(http: HttpSecurity) {
        http.addFilterBefore(postProcess(authenticationFilter(http.getSharedObject(ApplicationContext::class.java))), AuthorizationFilter::class.java)
    }

    private fun authenticationFilter(context: ApplicationContext?): AuthenticationFilter {
        val verifier = miniApp.verifier(context)
        val userDetailsService = miniApp.userDetailsService(context)
        val initDataResolver = miniApp.initDataResolver(context)
        val botTokenResolver = miniApp.botTokenResolver(context)
        val successHandler = miniApp.successHandler()
        traceSettings(verifier, userDetailsService, initDataResolver, botTokenResolver, successHandler)
        val provider = TelegramAuthenticationProvider(verifier, userDetailsService)
        val converter = TelegramAuthenticationConverter(initDataResolver, botTokenResolver)
        return AuthenticationFilter(ProviderManager(provider), converter).apply {
            this.successHandler = successHandler
            failureHandler = AuthenticationEntryPointFailureHandler(entryPoint)
        }
    }

    private fun traceSettings(vararg settings: Any) {
        if (log.isTraceEnabled) {
            log.trace("Telegram Mini App authentication: {}, entry point {}", settings.joinToString { it.javaClass.name }, entryPoint.javaClass.name)
        }
    }

    /** Java entry point: `http.with(TelegramConfigurer.telegram(), …)`. */
    public companion object {

        /** New configurer. */
        @JvmStatic
        public fun telegram(): TelegramConfigurer = TelegramConfigurer()

        private val log = LoggerFactory.getLogger(TelegramConfigurer::class.java)
    }
}
