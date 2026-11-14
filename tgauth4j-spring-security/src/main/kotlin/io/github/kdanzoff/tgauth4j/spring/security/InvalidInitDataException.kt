package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.InvalidReason
import org.springframework.security.authentication.BadCredentialsException

/**
 * `initData` refusé (401). Le message ne contient que la raison, jamais les données.
 *
 * @property reason raison du refus.
 */
public class InvalidInitDataException(public val reason: InvalidReason) : BadCredentialsException("init data rejected: $reason") {

    /** Code d'erreur pour le client : `init_data_expired` (rouvrir la Mini App) ou `init_data_invalid`. */
    public val errorCode: String
        get() = if (reason == InvalidReason.EXPIRED) "init_data_expired" else "init_data_invalid"
}
