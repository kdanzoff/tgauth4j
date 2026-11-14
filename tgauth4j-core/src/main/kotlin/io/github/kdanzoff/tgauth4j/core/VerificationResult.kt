package io.github.kdanzoff.tgauth4j.core

/** Result of [TelegramInitDataVerifier.verify]: a rejected `initData` never throws. */
public sealed interface VerificationResult {

    /** Authentic and fresh `initData`. */
    public data class Valid(val initData: TelegramInitData) : VerificationResult

    /** Rejected `initData`, for [reason]. */
    public data class Invalid(val reason: InvalidReason) : VerificationResult
}

/** Why an `initData` was rejected. */
public enum class InvalidReason {
    /** Blank, malformed, without `hash`, without a valid user, or dated in the future. */
    MALFORMED,

    /** Wrong signature: tampered with, or signed for another bot. */
    BAD_SIGNATURE,

    /** Too old: the Mini App must be reopened. */
    EXPIRED,
}
