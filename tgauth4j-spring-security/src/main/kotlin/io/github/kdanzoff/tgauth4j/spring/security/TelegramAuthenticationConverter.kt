package io.github.kdanzoff.tgauth4j.spring.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.authentication.AuthenticationConverter

/**
 * Reads the request's `initData` ([initDataResolver]) and the bot token ([botTokenResolver]). Without `initData`:
 * `null`, the request stays anonymous and the entry point answers 401.
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
