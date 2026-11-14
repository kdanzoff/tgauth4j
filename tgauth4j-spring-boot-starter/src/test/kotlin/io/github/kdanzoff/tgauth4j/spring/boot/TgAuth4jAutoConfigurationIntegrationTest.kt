package io.github.kdanzoff.tgauth4j.spring.boot

import io.github.kdanzoff.tgauth4j.core.BotToken
import io.github.kdanzoff.tgauth4j.core.TelegramInitDataVerifier
import io.github.kdanzoff.tgauth4j.spring.security.BotTokenResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.mock.web.MockHttpServletRequest

class TgAuth4jAutoConfigurationIntegrationTest {

    private val runner = ApplicationContextRunner().withConfiguration(AutoConfigurations.of(TgAuth4jAutoConfiguration::class.java))

    @Test
    fun `the bot token property gives a resolver of that token`() {
        runner.withPropertyValues("tgauth4j.bot-token=123456:SECRET").run { context ->
            assertThat(context.getBean(BotTokenResolver::class.java).resolve(MockHttpServletRequest())).isEqualTo(BotToken("123456:SECRET"))
            assertThat(context).hasSingleBean(TelegramInitDataVerifier::class.java)
        }
    }

    @Test
    fun `without bot token there is a verifier but no resolver`() {
        runner.run { context ->
            assertThat(context).hasSingleBean(TelegramInitDataVerifier::class.java)
            assertThat(context).doesNotHaveBean(BotTokenResolver::class.java)
        }
    }

    @Test
    fun `an application bean replaces the default one`() {
        val own = BotTokenResolver { BotToken("1:OWN") }
        runner.withPropertyValues("tgauth4j.bot-token=123456:SECRET").withBean(BotTokenResolver::class.java, { own }).run { context ->
            assertThat(context.getBean(BotTokenResolver::class.java)).isSameAs(own)
        }
    }
}
