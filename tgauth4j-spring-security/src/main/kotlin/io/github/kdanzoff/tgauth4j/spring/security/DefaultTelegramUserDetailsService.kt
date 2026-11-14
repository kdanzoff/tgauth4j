package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

/** Used when no [org.springframework.security.core.userdetails.UserDetailsService] is provided: every verified user gets `ROLE_USER`. */
public class DefaultTelegramUserDetailsService : TelegramUserDetailsService {

    override fun loadUserByInitData(initData: TelegramInitData): UserDetails =
        TelegramUserDetails(initData.user, listOf(SimpleGrantedAuthority(ROLE_USER)))

    override fun loadUserByUsername(username: String): UserDetails = throw UsernameNotFoundException("users are loaded from init data only")

    private companion object {
        const val ROLE_USER = "ROLE_USER"
    }
}
