package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.InvalidReason
import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import io.github.kdanzoff.tgauth4j.core.VerificationResult
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * Authentifie un [TelegramAuthenticationToken] : vérifie `initData` ([TelegramInitDataVerifier]), puis charge
 * l'utilisateur par [userDetailsService] — [TelegramUserDetailsService.loadUserByInitData] s'il en est un, sinon
 * `loadUserByUsername` avec l'identifiant Telegram — et contrôle son état ([userDetailsChecker]).
 *
 * `initData` refusé : [InvalidInitDataException] (401).
 */
public class TelegramAuthenticationProvider(
    private val verifier: TelegramInitDataVerifier,
    private val userDetailsService: UserDetailsService,
) : AuthenticationProvider {

    /** Contrôle du compte chargé (verrouillé, désactivé, expiré) ; Spring Security par défaut. */
    public var userDetailsChecker: UserDetailsChecker = AccountStatusUserDetailsChecker()

    override fun authenticate(authentication: Authentication): Authentication {
        val initData = verified(authentication as TelegramAuthenticationToken)
        val user = loadUser(initData)
        userDetailsChecker.check(user)
        return TelegramAuthenticationToken.authenticated(user, initData)
    }

    override fun supports(authentication: Class<*>): Boolean = TelegramAuthenticationToken::class.java.isAssignableFrom(authentication)

    private fun verified(token: TelegramAuthenticationToken): TelegramInitData {
        val raw = token.credentials as? String
        val key = token.verificationKey
        if (raw == null || key == null) throw InvalidInitDataException(InvalidReason.MALFORMED)
        return when (val result = verifier.verify(raw, key)) {
            is VerificationResult.Valid -> result.initData
            is VerificationResult.Invalid -> throw InvalidInitDataException(result.reason)
        }
    }

    private fun loadUser(initData: TelegramInitData): UserDetails = when (val users = userDetailsService) {
        is TelegramUserDetailsService -> users.loadUserByInitData(initData)
        else -> users.loadUserByUsername(initData.user.id.toString())
    }
}
