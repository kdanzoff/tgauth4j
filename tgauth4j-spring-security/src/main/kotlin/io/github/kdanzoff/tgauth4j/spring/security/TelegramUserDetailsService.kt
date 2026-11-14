package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * [UserDetailsService] qui reçoit tout l'`initData` vérifié, pas seulement l'identifiant.
 *
 * [TelegramAuthenticationProvider] appelle [loadUserByInitData] ; par défaut, il délègue à [loadUserByUsername] avec
 * l'identifiant Telegram. Un simple [UserDetailsService] convient aussi : il reçoit l'identifiant Telegram comme nom.
 */
public interface TelegramUserDetailsService : UserDetailsService {

    /** Utilisateur authentifié par [initData]. */
    public fun loadUserByInitData(initData: TelegramInitData): UserDetails = loadUserByUsername(initData.user.id.toString())
}
