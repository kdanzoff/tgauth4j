package io.github.kdanzoff.tgauth4j.spring.security

import io.github.kdanzoff.tgauth4j.core.BotToken
import io.github.kdanzoff.tgauth4j.core.TelegramInitData
import io.github.kdanzoff.tgauth4j.test.TelegramTestInitData.telegramInitData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mock.web.MockServletContext
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/** Chaîne Spring Security configurée par le DSL Kotlin ou l'API Java, de bout en bout avec MockMvc. */
class TelegramConfigurerIntegrationTest {

    private fun mvc(vararg configurations: Class<*>): MockMvc {
        val context = AnnotationConfigWebApplicationContext().apply {
            register(Base::class.java, *configurations)
            servletContext = MockServletContext()
            refresh()
        }
        return MockMvcBuilders.webAppContextSetup(context).apply<DefaultMockMvcBuilder>(springSecurity()).build()
    }

    private fun MockMvc.me(initData: (org.springframework.test.web.servlet.MockHttpServletRequestDsl.() -> Unit) = {}) = get("/me", dsl = initData)

    @Test
    fun `a valid init data authenticates the user with ROLE_USER by default`() {
        mvc(KotlinDslChain::class.java).me { with(telegramInitData(TOKEN) { userId = 42; authDate = NOW }) }
            .andExpect { status { isOk() }; content { string("42 [ROLE_USER]") } }
    }

    @Test
    fun `the Java API configures the same chain`() {
        mvc(JavaApiChain::class.java).me { with(telegramInitData(TOKEN) { userId = 42; authDate = NOW }) }
            .andExpect { status { isOk() }; content { string("42 [ROLE_USER]") } }
    }

    @Test
    fun `without init data the answer is 401 with the tma challenge`() {
        mvc(KotlinDslChain::class.java).me()
            .andExpect { status { isUnauthorized() }; header { string("WWW-Authenticate", "tma") } }
    }

    @Test
    fun `a refused init data is a 401 with its error code`() {
        val mvc = mvc(KotlinDslChain::class.java)

        mvc.me { with(telegramInitData(OTHER_TOKEN) { authDate = NOW }) }
            .andExpect { status { isUnauthorized() }; header { string("WWW-Authenticate", """tma error="init_data_invalid"""") } }
        mvc.me { with(telegramInitData(TOKEN) { authDate = NOW.minus(Duration.ofHours(2)) }) }
            .andExpect { status { isUnauthorized() }; header { string("WWW-Authenticate", """tma error="init_data_expired"""") } }
    }

    @Test
    fun `a TelegramUserDetailsService bean receives the whole init data`() {
        mvc(KotlinDslChain::class.java, TelegramUsers::class.java)
            .me { with(telegramInitData(TOKEN) { userId = 42; firstName = "Ada"; authDate = NOW }) }
            .andExpect { status { isOk() }; content { string("Ada [ROLE_ADMIN]") } }
    }

    @Test
    fun `a plain UserDetailsService bean receives the Telegram user id as username`() {
        mvc(KotlinDslChain::class.java, PlainUsers::class.java)
            .me { with(telegramInitData(TOKEN) { userId = 42; authDate = NOW }) }
            .andExpect { status { isOk() }; content { string("user-42 [ROLE_MEMBER]") } }
    }

    @Test
    fun `a locked account is a 401`() {
        mvc(KotlinDslChain::class.java, LockedUsers::class.java)
            .me { with(telegramInitData(TOKEN) { authDate = NOW }) }
            .andExpect { status { isUnauthorized() }; header { string("WWW-Authenticate", "tma") } }
    }

    @Test
    fun `the authentication keeps the verified init data but neither the raw one nor the key`() {
        val token = TelegramAuthenticationToken.authenticated(
            User.withUsername("42").password("").authorities("ROLE_USER").build(),
            TelegramInitData(io.github.kdanzoff.tgauth4j.core.TelegramUser(42, "Ada"), NOW),
        )

        assertThat(token.credentials).isNull()
        assertThat(token.verificationKey).isNull()
        assertThat(token.toString()).doesNotContain("Ada")
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    class Base {
        @Bean
        fun clock(): Clock = Clock.fixed(NOW, ZoneOffset.UTC)

        @Bean
        fun controller() = MeController()
    }

    @RestController
    class MeController {
        @GetMapping("/me")
        fun me(authentication: Authentication): String = "${authentication.name} ${authentication.authorities.map { it.authority }}"
    }

    @Configuration(proxyBeanMethods = false)
    class KotlinDslChain {
        @Bean
        fun chain(http: HttpSecurity): SecurityFilterChain {
            http {
                authorizeHttpRequests { authorize(anyRequest, authenticated) }
                telegram {
                    miniApp { botToken = BotToken(TOKEN) }
                }
            }
            return http.build()
        }
    }

    @Configuration(proxyBeanMethods = false)
    class JavaApiChain {
        @Bean
        fun chain(http: HttpSecurity): SecurityFilterChain = http
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .with(TelegramConfigurer.telegram()) { telegram -> telegram.miniApp { it.botToken(BotToken(TOKEN)) } }
            .build()
    }

    @Configuration(proxyBeanMethods = false)
    class TelegramUsers {
        @Bean
        fun users(): UserDetailsService = object : TelegramUserDetailsService {
            override fun loadUserByInitData(initData: TelegramInitData): UserDetails =
                User.withUsername(initData.user.firstName).password("").authorities("ROLE_ADMIN").build()

            override fun loadUserByUsername(username: String): UserDetails = error("not used")
        }
    }

    @Configuration(proxyBeanMethods = false)
    class PlainUsers {
        @Bean
        fun users(): UserDetailsService = UserDetailsService { username ->
            User.withUsername("user-$username").password("").authorities(SimpleGrantedAuthority("ROLE_MEMBER")).build()
        }
    }

    @Configuration(proxyBeanMethods = false)
    class LockedUsers {
        @Bean
        fun users(): UserDetailsService = UserDetailsService { username ->
            User.withUsername(username).password("").authorities("ROLE_USER").accountLocked(true).build()
        }
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-13T10:00:00Z")
        const val TOKEN = "123456:TEST-TOKEN"
        const val OTHER_TOKEN = "654321:OTHER-TOKEN"
    }
}
