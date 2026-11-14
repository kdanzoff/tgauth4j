package io.github.kdanzoff.tgauth4j.spring.security

import jakarta.servlet.http.HttpServletRequest

/** Lecture de l'`initData` brut d'une requête. */
public fun interface InitDataResolver {

    /** `initData` de [request], ou `null` s'il n'y en a pas (la requête reste anonyme). */
    public fun resolve(request: HttpServletRequest): String?
}

/** `Authorization: tma <initData>`, le schéma recommandé par Telegram ; autre schéma ou absence : `null`. */
public class HeaderInitDataResolver : InitDataResolver {

    override fun resolve(request: HttpServletRequest): String? =
        request.getHeader(AUTHORIZATION)?.takeIf { it.startsWith(SCHEME) }?.removePrefix(SCHEME)

    private companion object {
        const val AUTHORIZATION = "Authorization"
        const val SCHEME = "tma "
    }
}
