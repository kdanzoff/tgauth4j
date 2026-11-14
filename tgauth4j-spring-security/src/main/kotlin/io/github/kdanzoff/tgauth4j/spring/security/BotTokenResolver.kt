package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.BotToken
import jakarta.servlet.http.HttpServletRequest

/** Token of the bot that signed the `initData` of a request: a single one per backend with [DefaultBotTokenResolver]. */
public fun interface BotTokenResolver {

    /** Bot token for [request]. */
    public fun resolve(request: HttpServletRequest): BotToken
}

/** Single bot: always [token]. */
public class DefaultBotTokenResolver(private val token: BotToken) : BotTokenResolver {

    override fun resolve(request: HttpServletRequest): BotToken = token
}
