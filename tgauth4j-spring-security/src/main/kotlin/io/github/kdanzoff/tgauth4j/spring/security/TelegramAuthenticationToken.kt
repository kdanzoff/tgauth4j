package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import io.github.kdanzoff.tgauth4j.core.VerificationKey
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails

/**
 * Authentification Spring Security d'un utilisateur de Mini App.
 *
 * - avant vérification ([unauthenticated]) : `initData` brut et clé de vérification ;
 * - après vérification ([authenticated]) : le [UserDetails] chargé, ses rôles et l'[initData] vérifié.
 *
 * `initData` brut et clé sont effacés après l'authentification ([eraseCredentials]) ; [toString] ne montre que le nom.
 */
public class TelegramAuthenticationToken private constructor(
    private var rawInitData: String?,
    private var key: VerificationKey?,
    private val user: UserDetails?,
    /** `initData` vérifié ; `null` avant vérification. */
    public val initData: TelegramInitData?,
) : AbstractAuthenticationToken(user?.authorities ?: emptyList()) {

    /** Clé de vérification ; `null` après vérification. */
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

    /** Fabriques : avant et après vérification. */
    public companion object {

        /** `initData` reçu, à vérifier avec [key]. */
        @JvmStatic
        public fun unauthenticated(initData: String, key: VerificationKey): TelegramAuthenticationToken =
            TelegramAuthenticationToken(initData, key, null, null)

        /** Utilisateur [user] authentifié par [initData]. */
        @JvmStatic
        public fun authenticated(user: UserDetails, initData: TelegramInitData): TelegramAuthenticationToken =
            TelegramAuthenticationToken(null, null, user, initData).apply { isAuthenticated = true }
    }
}
