package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.BotToken
import jakarta.servlet.http.HttpServletRequest

/** Jeton du bot qui a signé l'`initData` de [resolve] : un seul par backend avec [DefaultBotTokenResolver]. */
public fun interface BotTokenResolver {

    /** Jeton du bot de [request]. */
    public fun resolve(request: HttpServletRequest): BotToken
}

/** Un seul bot : toujours [token]. */
public class DefaultBotTokenResolver(private val token: BotToken) : BotTokenResolver {

    override fun resolve(request: HttpServletRequest): BotToken = token
}
