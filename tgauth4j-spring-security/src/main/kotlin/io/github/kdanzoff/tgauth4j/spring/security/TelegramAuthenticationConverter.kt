package io.github.kdanzoff.tgauth4j.spring.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.authentication.AuthenticationConverter

/**
 * Lit l'`initData` de la requête ([initDataResolver]) et le jeton du bot ([botTokenResolver]). Sans `initData` :
 * `null`, la requête reste anonyme et le point d'entrée répond 401.
 */
public class TelegramAuthenticationConverter(
    private val initDataResolver: InitDataResolver,
    private val botTokenResolver: BotTokenResolver,
) : AuthenticationConverter {

    override fun convert(request: HttpServletRequest): TelegramAuthenticationToken? {
        val initData = initDataResolver.resolve(request) ?: return null
        return TelegramAuthenticationToken.unauthenticated(initData, botTokenResolver.resolve(request))
    }
}
