package io.github.kdanzoff.tgauth4j.spring.security

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
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
        val initData = initDataResolver.resolve(request)
        if (initData == null) {
            log.trace("No initData in {} {}, the request stays anonymous", request.method, request.requestURI)
            return null
        }
        val key = botTokenResolver.resolve(request)
        log.trace("initData found in {} {} ({} characters), to verify for bot {}", request.method, request.requestURI, initData.length, key.botId)
        return TelegramAuthenticationToken.unauthenticated(initData, key)
    }

    private companion object {
        private val log = LoggerFactory.getLogger(TelegramAuthenticationConverter::class.java)
    }
}
