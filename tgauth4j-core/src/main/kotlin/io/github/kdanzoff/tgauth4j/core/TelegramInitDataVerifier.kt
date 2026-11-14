package io.github.kdanzoff.tgauth4j.core

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Verifies `initData`, the string Telegram signs when a Mini App is opened.
 *
 * With a [BotToken]:
 * 1. the secret key is `HMAC-SHA256(key = "WebAppData", message = bot token)`;
 * 2. the expected signature is `HMAC-SHA256(key = secret, message = fields sorted as "key=value" separated by a line
 *    feed, without hash)`, in hexadecimal, compared in **constant time**;
 * 3. `auth_date` must not be older than [maxAge], nor in the future beyond one minute of clock skew.
 *
 * Configuration goes to the constructor, request data to the call. Nothing read is logged or included in a message:
 * `initData` contains the user's name.
 *
 * @param maxAge maximum age of `initData` (1 hour by default).
 * @param clock clock, for tests.
 */
public class TelegramInitDataVerifier @JvmOverloads constructor(
    private val maxAge: Duration = DEFAULT_MAX_AGE,
    private val clock: Clock = Clock.systemUTC(),
) {

    init {
        require(!maxAge.isNegative && !maxAge.isZero) { "maxAge must be positive" }
    }

    /** Verifies [initData] with [key]; a rejection is a [VerificationResult.Invalid], never an exception. */
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

    /** Default values. */
    public companion object {
        /** Default maximum age of `initData`. */
        @JvmField
        public val DEFAULT_MAX_AGE: Duration = Duration.ofHours(1)

        private const val HASH = "hash"
        private const val AUTH_DATE = "auth_date"
        private const val USER = "user"
        private val CLOCK_SKEW: Duration = Duration.ofMinutes(1)
    }
}
