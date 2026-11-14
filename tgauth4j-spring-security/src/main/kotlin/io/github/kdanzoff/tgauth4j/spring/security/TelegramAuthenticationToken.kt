package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import io.github.kdanzoff.tgauth4j.core.VerificationKey
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security authentication of a Mini App user.
 *
 * - before verification ([unauthenticated]): raw `initData` and verification key;
 * - after verification ([authenticated]): the loaded [UserDetails], its authorities and the verified [initData].
 *
 * Raw `initData` and key are erased after authentication ([eraseCredentials]); [toString] shows the name only.
 */
public class TelegramAuthenticationToken private constructor(
    private var rawInitData: String?,
    private var key: VerificationKey?,
    private val user: UserDetails?,
    /** Verified `initData`; `null` before verification. */
    public val initData: TelegramInitData?,
) : AbstractAuthenticationToken(user?.authorities ?: emptyList()) {

    /** Verification key; `null` after verification. */
    public val verificationKey: VerificationKey? get() = key

    override fun getCredentials(): Any? = rawInitData

    override fun getPrincipal(): Any? = user

    override fun getName(): String = user?.username ?: ""

    override fun eraseCredentials() {
        super.eraseCredentials()
        rawInitData = null
        key = null
    }

    override fun toString(): String = "TelegramAuthenticationToken(name=$name, authenticated=$isAuthenticated, authorities=$authorities)"

    /** Factories: before and after verification. */
    public companion object {

        /** Received `initData`, to be verified with [key]. */
        @JvmStatic
        public fun unauthenticated(initData: String, key: VerificationKey): TelegramAuthenticationToken =
            TelegramAuthenticationToken(initData, key, null, null)

        /** User [user] authenticated by [initData]. */
        @JvmStatic
        public fun authenticated(user: UserDetails, initData: TelegramInitData): TelegramAuthenticationToken =
            TelegramAuthenticationToken(null, null, user, initData).apply { isAuthenticated = true }
    }
}
