package io.github.kdanzoff.tgauth4j.spring.security

import jakarta.servlet.http.HttpServletRequest

/** Reads the raw `initData` of a request. */
public fun interface InitDataResolver {

    /** `initData` of [request], or `null` if there is none (the request stays anonymous). */
    public fun resolve(request: HttpServletRequest): String?
}

/** `Authorization: tma <initData>`, the scheme recommended by Telegram; another scheme or no header: `null`. */
public class HeaderInitDataResolver : InitDataResolver {

    override fun resolve(request: HttpServletRequest): String? =
        request.getHeader(AUTHORIZATION)?.takeIf { it.startsWith(SCHEME) }?.removePrefix(SCHEME)

    private companion object {
        const val AUTHORIZATION = "Authorization"
        const val SCHEME = "tma "
    }
}
