package io.github.kdanzoff.tgauth4j.core

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class BotTokenTest {

    @Test
    fun `the bot id is the part before the colon`() {
        assertThat(BotToken("123456:SECRET").botId).isEqualTo(123456)
    }

    @Test
    fun `the secret never appears in toString`() {
        assertThat(BotToken("123456:SECRET").toString()).isEqualTo("BotToken(botId=123456)")
    }

    @Test
    fun `two tokens are equal only with the same secret`() {
        assertThat(BotToken("1:A")).isEqualTo(BotToken("1:A")).isNotEqualTo(BotToken("1:B"))
    }

    @Test
    fun `a value that is not a bot token is refused`() {
        listOf("", "SECRET", ":SECRET", "abc:SECRET", "123456:", "-1:SECRET", "0:SECRET").forEach { value ->
            assertThatThrownBy { BotToken(value) }.describedAs(value).isInstanceOf(IllegalArgumentException::class.java)
        }
    }
}
