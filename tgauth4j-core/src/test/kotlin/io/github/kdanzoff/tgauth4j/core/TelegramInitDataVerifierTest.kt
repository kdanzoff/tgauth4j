package io.github.kdanzoff.tgauth4j.core

import io.github.kdanzoff.tgauth4j.test.TelegramTestInitData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class TelegramInitDataVerifierTest {

    private val now = Instant.parse("2026-11-13T10:00:00Z")
    private val key = BotToken(TOKEN)
    private val verifier = TelegramInitDataVerifier(Duration.ofHours(1), Clock.fixed(now, ZoneOffset.UTC))

    private fun signed(token: String = TOKEN, authDate: Instant = now, userId: Long = 1001) =
        TelegramTestInitData.signed(token) {
            this.userId = userId
            this.authDate = authDate
            firstName = "Jean"
            username = "jean_secret"
            startParam = "lease-42"
        }

    private fun reasonOf(initData: String): InvalidReason? = (verifier.verify(initData, key) as? VerificationResult.Invalid)?.reason

    @Test
    fun `a valid init data gives the user, the signature date and the other fields`() {
        val initData = (verifier.verify(signed(), key) as VerificationResult.Valid).initData

        assertThat(initData.user.id).isEqualTo(1001)
        assertThat(initData.user.firstName).isEqualTo("Jean")
        assertThat(initData.user.username).isEqualTo("jean_secret")
        assertThat(initData.authDate).isEqualTo(now)
        assertThat(initData.startParam).isEqualTo("lease-42")
        assertThat(initData.queryId).isNotBlank()
    }

    @Test
    fun `personal data never appears in toString`() {
        val result = verifier.verify(signed(), key).toString()

        assertThat(result).contains("1001").doesNotContain("Jean").doesNotContain("jean_secret")
    }

    @Test
    fun `an altered init data or one signed for another bot is a bad signature`() {
        assertThat(reasonOf(signed().replace("1001", "1002"))).isEqualTo(InvalidReason.BAD_SIGNATURE)
        assertThat(reasonOf(signed(token = OTHER_TOKEN))).isEqualTo(InvalidReason.BAD_SIGNATURE)
    }

    @Test
    fun `an old init data is expired, one from the future is malformed`() {
        assertThat(reasonOf(signed(authDate = now.minus(Duration.ofHours(2))))).isEqualTo(InvalidReason.EXPIRED)
        assertThat(reasonOf(signed(authDate = now.plus(Duration.ofHours(1))))).isEqualTo(InvalidReason.MALFORMED)
    }

    @Test
    fun `a malformed init data is refused without any exception`() {
        listOf("", "garbage", "a=b", "=x", "a=%zz").forEach { assertThat(reasonOf(it)).describedAs(it).isEqualTo(InvalidReason.MALFORMED) }
    }

    @Test
    fun `a signed init data whose user has no valid id is malformed`() {
        assertThat(reasonOf(signed(userId = 0))).isEqualTo(InvalidReason.MALFORMED)
    }

    @Test
    fun `the maximum age must be positive`() {
        org.assertj.core.api.Assertions.assertThatThrownBy { TelegramInitDataVerifier(Duration.ZERO) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    private companion object {
        const val TOKEN = "123456:TEST-TOKEN"
        const val OTHER_TOKEN = "654321:OTHER-TOKEN"
    }
}
