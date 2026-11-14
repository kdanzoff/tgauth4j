package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Default user ([DefaultTelegramUserDetailsService]): the Telegram user and its [authorities], without a password.
 * The username is the Telegram id.
 *
 * @property user verified Telegram user.
 */
public class TelegramUserDetails(
    public val user: TelegramUser,
    private val authorities: Collection<GrantedAuthority>,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String? = null

    override fun getUsername(): String = user.id.toString()

    override fun toString(): String = "TelegramUserDetails(id=${user.id}, authorities=$authorities)"
}
