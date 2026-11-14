package io.github.kdanzoff.tgauth4j.spring.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

/** Après authentification : ni redirection ni session, la requête continue (comportement par défaut d'une API). */
internal object ContinueRequestSuccessHandler : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication): Unit = Unit
}
