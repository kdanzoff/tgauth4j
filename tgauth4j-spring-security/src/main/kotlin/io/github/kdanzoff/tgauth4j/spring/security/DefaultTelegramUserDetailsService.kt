package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

/** Sans [org.springframework.security.core.userdetails.UserDetailsService] fourni : tout utilisateur vérifié a `ROLE_USER`. */
public class DefaultTelegramUserDetailsService : TelegramUserDetailsService {

    override fun loadUserByInitData(initData: TelegramInitData): UserDetails =
        TelegramUserDetails(initData.user, listOf(SimpleGrantedAuthority(ROLE_USER)))

    override fun loadUserByUsername(username: String): UserDetails = throw UsernameNotFoundException("users are loaded from init data only")

    private companion object {
        const val ROLE_USER = "ROLE_USER"
    }
}
