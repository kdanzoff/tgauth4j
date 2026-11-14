package io.github.kdanzoff.tgauth4j.core

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Vérifie `initData`, la chaîne que Telegram signe à l'ouverture d'une Mini App.
 *
 * Avec un [BotToken] :
 * 1. la clé secrète est `HMAC-SHA256(clé = "WebAppData", message = jeton du bot)` ;
 * 2. la signature attendue est `HMAC-SHA256(clé = secret, message = champs triés « clé=valeur » séparés par un saut
 *    de ligne, sans hash)`, en hexadécimal, comparée en **temps constant** ;
 * 3. `auth_date` ne doit pas dater de plus de [maxAge], ni du futur au-delà d'une minute de décalage d'horloge.
 *
 * Configuration au constructeur, données de la requête à l'appel. Rien de ce qui est lu n'est journalisé ni repris
 * dans un message : `initData` contient le nom de l'utilisateur.
 *
 * @param maxAge âge maximal d'`initData` (1 heure par défaut).
 * @param clock horloge, pour les tests.
 */
public class TelegramInitDataVerifier @JvmOverloads constructor(
    private val maxAge: Duration = DEFAULT_MAX_AGE,
    private val clock: Clock = Clock.systemUTC(),
) {

    init {
        require(!maxAge.isNegative && !maxAge.isZero) { "maxAge must be positive" }
    }

    /** Vérifie [initData] avec [key] ; un refus est un [VerificationResult.Invalid], jamais une exception. */
    public fun verify(initData: String, key: VerificationKey): VerificationResult {
        val fields = InitDataFields.parse(initData) ?: return invalid(InvalidReason.MALFORMED)
        return when (key) {
            is BotToken -> verifyHmac(fields, key)
        }
    }

    private fun verifyHmac(fields: Map<String, String>, key: BotToken): VerificationResult {
        val received = fields[HASH] ?: return invalid(InvalidReason.MALFORMED)
        val expected = Hmac.hex(Hmac.sha256(key.webAppSecret, InitDataFields.dataCheckString(fields, HASH).toByteArray(Charsets.UTF_8)))
        if (!Hmac.sameHex(expected, received)) return invalid(InvalidReason.BAD_SIGNATURE)
        return readSigned(fields)
    }

    private fun readSigned(fields: Map<String, String>): VerificationResult {
        val authDate = fields[AUTH_DATE]?.toLongOrNull()?.let(Instant::ofEpochSecond) ?: return invalid(InvalidReason.MALFORMED)
        freshnessProblem(authDate)?.let { return invalid(it) }
        val user = fields[USER]?.let(TelegramUserParser::parse) ?: return invalid(InvalidReason.MALFORMED)
        return VerificationResult.Valid(initDataOf(fields, user, authDate))
    }

    private fun freshnessProblem(authDate: Instant): InvalidReason? {
        val now = clock.instant()
        return when {
            authDate.isAfter(now.plus(CLOCK_SKEW)) -> InvalidReason.MALFORMED
            Duration.between(authDate, now) > maxAge -> InvalidReason.EXPIRED
            else -> null
        }
    }

    private fun initDataOf(fields: Map<String, String>, user: TelegramUser, authDate: Instant) = TelegramInitData(
        user = user,
        authDate = authDate,
        queryId = fields["query_id"],
        startParam = fields["start_param"],
        chatType = fields["chat_type"],
        chatInstance = fields["chat_instance"],
        fields = fields,
    )

    private fun invalid(reason: InvalidReason) = VerificationResult.Invalid(reason)

    /** Valeurs par défaut. */
    public companion object {
        /** Âge maximal par défaut d'`initData`. */
        @JvmField
        public val DEFAULT_MAX_AGE: Duration = Duration.ofHours(1)

        private const val HASH = "hash"
        private const val AUTH_DATE = "auth_date"
        private const val USER = "user"
        private val CLOCK_SKEW: Duration = Duration.ofMinutes(1)
    }
}
