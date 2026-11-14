package io.github.kdanzoff.tgauth4j.spring.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

/**
 * Default 401, without a body: `WWW-Authenticate: tma`, with `error="init_data_invalid"` or
 * `error="init_data_expired"` when `initData` was rejected ([InvalidInitDataException.errorCode]).
 */
public class TelegramAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(request: HttpServletRequest, response: HttpServletResponse, failure: AuthenticationException) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.setHeader(WWW_AUTHENTICATE, challenge(failure))
    }

    private fun challenge(failure: AuthenticationException): String =
        (failure as? InvalidInitDataException)?.let { """$SCHEME error="${it.errorCode}"""" } ?: SCHEME

    private companion object {
        const val WWW_AUTHENTICATE = "WWW-Authenticate"
        const val SCHEME = "tma"
    }
}
