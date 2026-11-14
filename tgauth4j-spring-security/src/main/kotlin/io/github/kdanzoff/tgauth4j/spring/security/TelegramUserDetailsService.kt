package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * [UserDetailsService] that receives the whole verified `initData`, not just the id.
 *
 * [TelegramAuthenticationProvider] calls [loadUserByInitData]; by default it delegates to [loadUserByUsername] with
 * the Telegram id. A plain [UserDetailsService] also works: it receives the Telegram id as username.
 */
public interface TelegramUserDetailsService : UserDetailsService {

    /** User authenticated by [initData]. */
    public fun loadUserByInitData(initData: TelegramInitData): UserDetails = loadUserByUsername(initData.user.id.toString())
}
