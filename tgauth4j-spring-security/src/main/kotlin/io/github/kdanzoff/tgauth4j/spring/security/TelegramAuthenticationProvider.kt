package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.InvalidReason
import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import io.github.kdanzoff.tgauth4j.core.VerificationResult
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * Authenticates a [TelegramAuthenticationToken]: verifies `initData` ([TelegramInitDataVerifier]), then loads the
 * user through [userDetailsService] — [TelegramUserDetailsService.loadUserByInitData] if it is one, otherwise
 * `loadUserByUsername` with the Telegram id — and checks its status ([userDetailsChecker]).
 *
 * Rejected `initData`: [InvalidInitDataException] (401).
 */
public class TelegramAuthenticationProvider(
    private val verifier: TelegramInitDataVerifier,
    private val userDetailsService: UserDetailsService,
) : AuthenticationProvider {

    /** Checks the loaded account (locked, disabled, expired); Spring Security's checker by default. */
    public var userDetailsChecker: UserDetailsChecker = AccountStatusUserDetailsChecker()

    override fun authenticate(authentication: Authentication): Authentication {
        val initData = verified(authentication as TelegramAuthenticationToken)
        val user = loadUser(initData)
        userDetailsChecker.check(user)
        log.trace("Telegram user {} authenticated as '{}' with {}", initData.user.id, user.username, user.authorities)
        return TelegramAuthenticationToken.authenticated(user, initData)
    }

    override fun supports(authentication: Class<*>): Boolean = TelegramAuthenticationToken::class.java.isAssignableFrom(authentication)

    private fun verified(token: TelegramAuthenticationToken): TelegramInitData {
        val raw = token.credentials as? String
        val key = token.verificationKey
        if (raw == null || key == null) throw InvalidInitDataException(InvalidReason.MALFORMED)
        return when (val result = verifier.verify(raw, key)) {
            is VerificationResult.Valid -> result.initData
            is VerificationResult.Invalid -> throw InvalidInitDataException(result.reason).also {
                log.trace("initData rejected ({}), answering {}", result.reason, it.errorCode)
            }
        }
    }

    private fun loadUser(initData: TelegramInitData): UserDetails = when (val users = userDetailsService) {
        is TelegramUserDetailsService -> {
            log.trace("Loading Telegram user {} with {}.loadUserByInitData", initData.user.id, users.javaClass.name)
            users.loadUserByInitData(initData)
        }
        else -> {
            log.trace("Loading Telegram user {} with {}.loadUserByUsername", initData.user.id, users.javaClass.name)
            users.loadUserByUsername(initData.user.id.toString())
        }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(TelegramAuthenticationProvider::class.java)
    }
}
