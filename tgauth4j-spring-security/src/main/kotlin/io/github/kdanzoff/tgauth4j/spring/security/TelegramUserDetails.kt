package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Utilisateur par défaut ([DefaultTelegramUserDetailsService]) : l'utilisateur Telegram et ses [authorities], sans mot
 * de passe. Le nom est l'identifiant Telegram.
 *
 * @property user utilisateur Telegram vérifié.
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
