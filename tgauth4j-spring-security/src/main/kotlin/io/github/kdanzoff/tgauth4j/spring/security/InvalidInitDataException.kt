package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.InvalidReason
import org.springframework.security.authentication.BadCredentialsException

/**
 * Rejected `initData` (401). The message contains the reason only, never the data.
 *
 * @property reason why it was rejected.
 */
public class InvalidInitDataException(public val reason: InvalidReason) : BadCredentialsException("init data rejected: $reason") {

    /** Error code for the client: `init_data_expired` (reopen the Mini App) or `init_data_invalid`. */
    public val errorCode: String
        get() = if (reason == InvalidReason.EXPIRED) "init_data_expired" else "init_data_invalid"
}
