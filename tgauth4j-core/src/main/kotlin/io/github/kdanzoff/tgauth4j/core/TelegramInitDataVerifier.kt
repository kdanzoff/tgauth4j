package io.github.kdanzoff.tgauth4j.core

import org.slf4j.LoggerFactory
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
 * Configuration goes to the constructor, request data to the call. `initData` contains the user's name: at `TRACE`
 * level, only the field names, the bot id, the user id, `auth_date` and the reason of a rejection are logged.
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
        val fields = InitDataFields.parse(initData) ?: return invalid(InvalidReason.MALFORMED) { "blank or not a URL-encoded key=value list" }
        if (log.isTraceEnabled) log.trace("Verifying initData for bot {}, fields {}", key.botId, fields.keys.sorted())
        return when (key) {
            is BotToken -> verifyHmac(fields, key)
        }
    }

    private fun verifyHmac(fields: Map<String, String>, key: BotToken): VerificationResult {
        val received = fields[HASH] ?: return invalid(InvalidReason.MALFORMED) { "no $HASH field" }
        val expected = Hmac.hex(Hmac.sha256(key.webAppSecret, InitDataFields.dataCheckString(fields, HASH).toByteArray(Charsets.UTF_8)))
        if (!Hmac.sameHex(expected, received)) {
            return invalid(InvalidReason.BAD_SIGNATURE) { "signature mismatch: tampered with, or not signed with the token of bot ${key.botId}" }
        }
        return readSigned(fields)
    }

    private fun readSigned(fields: Map<String, String>): VerificationResult {
        val authDate = fields[AUTH_DATE]?.toLongOrNull()?.let(Instant::ofEpochSecond)
            ?: return invalid(InvalidReason.MALFORMED) { "$AUTH_DATE missing or not a number of seconds" }
        freshnessProblem(authDate)?.let { return it }
        val user = fields[USER]?.let(TelegramUserParser::parse)
            ?: return invalid(InvalidReason.MALFORMED) { "$USER missing or invalid (a positive id and a first_name are required)" }
        log.trace("initData verified: user {}, {} {}", user.id, AUTH_DATE, authDate)
        return VerificationResult.Valid(initDataOf(fields, user, authDate))
    }

    private fun freshnessProblem(authDate: Instant): VerificationResult.Invalid? {
        val now = clock.instant()
        return when {
            authDate.isAfter(now.plus(CLOCK_SKEW)) ->
                invalid(InvalidReason.MALFORMED) { "$AUTH_DATE $authDate is in the future (now $now, allowed clock skew $CLOCK_SKEW)" }
            Duration.between(authDate, now) > maxAge ->
                invalid(InvalidReason.EXPIRED) { "$AUTH_DATE $authDate is older than maxAge $maxAge (now $now)" }
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

    /** Logs why `initData` is rejected; [detail] is only built when `TRACE` is enabled. */
    private inline fun invalid(reason: InvalidReason, detail: () -> String): VerificationResult.Invalid {
        if (log.isTraceEnabled) log.trace("initData rejected as {}: {}", reason, detail())
        return VerificationResult.Invalid(reason)
    }

    /** Default values. */
    public companion object {
        /** Default maximum age of `initData`. */
        @JvmField
        public val DEFAULT_MAX_AGE: Duration = Duration.ofHours(1)

        private const val HASH = "hash"
        private const val AUTH_DATE = "auth_date"
        private const val USER = "user"
        private val CLOCK_SKEW: Duration = Duration.ofMinutes(1)
        private val log = LoggerFactory.getLogger(TelegramInitDataVerifier::class.java)
    }
}
