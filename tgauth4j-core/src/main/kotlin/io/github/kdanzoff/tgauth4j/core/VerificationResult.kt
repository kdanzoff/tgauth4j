package io.github.kdanzoff.tgauth4j.core

/** Résultat de [TelegramInitDataVerifier.verify] : jamais d'exception pour un `initData` refusé. */
public sealed interface VerificationResult {

    /** `initData` authentique et récent. */
    public data class Valid(val initData: TelegramInitData) : VerificationResult

    /** `initData` refusé, pour la raison [reason]. */
    public data class Invalid(val reason: InvalidReason) : VerificationResult
}

/** Raison du refus d'un `initData`. */
public enum class InvalidReason {
    /** Vide, mal formé, sans `hash`, sans utilisateur valide ou daté du futur. */
    MALFORMED,

    /** Signature fausse : altéré, ou signé pour un autre bot. */
    BAD_SIGNATURE,

    /** Trop ancien : la Mini App doit être rouverte. */
    EXPIRED,
}
