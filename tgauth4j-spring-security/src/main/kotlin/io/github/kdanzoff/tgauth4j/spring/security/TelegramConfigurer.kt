package io.github.kdanzoff.tgauth4j.spring.security

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
 * Configurateur Spring Security de l'authentification Telegram, sur le modèle d'`OAuth2ResourceServerConfigurer` :
 *
 * ```java
 * http.with(TelegramConfigurer.telegram(), telegram -> telegram
 *     .miniApp(miniApp -> miniApp.botToken(new BotToken(token)))
 *     .authenticationEntryPoint(entryPoint));
 * ```
 *
 * En Kotlin : `http { telegram { miniApp { … } } }` ([telegram]).
 *
 * - `init` : [TelegramAuthenticationEntryPoint] (ou [authenticationEntryPoint]) devient le point d'entrée **par
 *   défaut** de la chaîne ; un point d'entrée déclaré dans `exceptionHandling` reste prioritaire ;
 * - `configure` : un `AuthenticationFilter` (lecture par [TelegramAuthenticationConverter], vérification par
 *   [TelegramAuthenticationProvider]) avant l'autorisation des requêtes, sans redirection ni session ; un refus
 *   d'`initData` passe par le même point d'entrée.
 *
 * Seules les Mini Apps sont prises en charge pour l'instant ([miniApp]).
 */
public class TelegramConfigurer : AbstractHttpConfigurer<TelegramConfigurer, HttpSecurity>() {

    private val miniApp = MiniAppConfigurer()
    private var entryPoint: AuthenticationEntryPoint = TelegramAuthenticationEntryPoint()

    /** Réglages des Mini Apps ([MiniAppConfigurer]). */
    public fun miniApp(customizer: Customizer<MiniAppConfigurer>): TelegramConfigurer = apply { customizer.customize(miniApp) }

    /** Réponse à une requête non authentifiée ou à un `initData` refusé (401). */
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
        val provider = TelegramAuthenticationProvider(miniApp.verifier(context), miniApp.userDetailsService(context))
        val converter = TelegramAuthenticationConverter(miniApp.initDataResolver(context), miniApp.botTokenResolver(context))
        return AuthenticationFilter(ProviderManager(provider), converter).apply {
            successHandler = miniApp.successHandler()
            failureHandler = AuthenticationEntryPointFailureHandler(entryPoint)
        }
    }

    /** Point d'entrée Java : `http.with(TelegramConfigurer.telegram(), …)`. */
    public companion object {

        /** Nouveau configurateur. */
        @JvmStatic
        public fun telegram(): TelegramConfigurer = TelegramConfigurer()
    }
}
