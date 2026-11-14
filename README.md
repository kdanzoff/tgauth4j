# tgauth4j

Telegram Mini Apps and Login Widget authentication for the JVM: init data verification, Spring Security integration
and Spring Boot starter.

> Status: `0.1.1`, Mini Apps only. Login Widget support is planned. Not yet on Maven Central: install it locally with
> `./gradlew publishToMavenLocal`.

Requirements: Java 17+, Spring Security 7 / Spring Boot 4 for the Spring modules.

## Modules

| Artifact | Content |
|---|---|
| `tgauth4j-core` | `initData` verification (HMAC-SHA256, constant-time comparison, `auth_date` freshness), no Spring dependency |
| `tgauth4j-spring-security` | `TelegramConfigurer`, Kotlin DSL `telegram { }`, authentication provider, token, entry point |
| `tgauth4j-spring-boot-starter` | Auto-configuration from `tgauth4j.*` properties |
| `tgauth4j-test` | Signed test `initData` and a MockMvc `RequestPostProcessor` |

Group: `io.github.kdanzoff`.

## Spring Boot

```kotlin
repositories { mavenLocal() }
dependencies {
    implementation("io.github.kdanzoff:tgauth4j-spring-boot-starter:0.1.1")
    testImplementation("io.github.kdanzoff:tgauth4j-test:0.1.1")
}
```

```yaml
tgauth4j:
  bot-token: ${TELEGRAM_BOT_TOKEN}
  init-data-max-age: 1h
```

```kotlin
@Bean
fun api(http: HttpSecurity): SecurityFilterChain {
    http {
        securityMatcher("/api/**")
        authorizeHttpRequests { authorize(anyRequest, authenticated) }
        telegram { }                       // bot token and verifier come from the starter
        sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
        csrf { disable() }
    }
    return http.build()
}
```

The Mini App sends `Authorization: tma <initData>` (`window.Telegram.WebApp.initData`).

- Valid `initData`: the request is authenticated; the name is the Telegram user id.
- Missing: `401` with `WWW-Authenticate: tma`.
- Refused: `401` with `WWW-Authenticate: tma error="init_data_invalid"` or `error="init_data_expired"`.

### Loading users

Any `UserDetailsService` bean is used, with the Telegram user id as username. Implement `TelegramUserDetailsService`
to receive the whole verified `initData`:

```kotlin
@Bean
fun users(accounts: Accounts) = object : TelegramUserDetailsService {
    override fun loadUserByInitData(initData: TelegramInitData): UserDetails = accounts.load(initData.user.id)
    override fun loadUserByUsername(username: String): UserDetails = accounts.load(username.toLong())
}
```

Without a `UserDetailsService`, every verified user gets `ROLE_USER`.

### Settings

```kotlin
telegram {
    miniApp {
        botToken = BotToken(token)                 // or botTokenResolver = BotTokenResolver { request -> … }
        maxAge = Duration.ofMinutes(30)            // or verifier = TelegramInitDataVerifier(…)
        initDataResolver = HeaderInitDataResolver()
        userDetailsService = users
        authenticationSuccessHandler = handler     // must let the request continue
    }
    authenticationEntryPoint = entryPoint          // e.g. problem+json responses
}
```

Each missing setting is taken from the application context (bean of the same type), then from a default.

Java:

```java
http.with(TelegramConfigurer.telegram(), telegram -> telegram
    .miniApp(miniApp -> miniApp.botToken(new BotToken(token))));
```

## Without Spring

```kotlin
val verifier = TelegramInitDataVerifier(Duration.ofHours(1))
when (val result = verifier.verify(initData, BotToken(token))) {
    is VerificationResult.Valid -> result.initData.user.id
    is VerificationResult.Invalid -> result.reason          // MALFORMED, BAD_SIGNATURE, EXPIRED
}
```

## Tests

```kotlin
mockMvc.get("/api/me") { with(TelegramTestInitData.telegramInitData(BOT_TOKEN) { userId = 42 }) }
```

## Security notes

- Only a key derived from the bot token is kept in memory; `BotToken.toString()` shows the bot id only.
- `initData` contains personal data: `toString()` of `TelegramUser`, `TelegramInitData` and the authentication token
  shows ids only, and nothing read is logged.
- Raw `initData` and the key are erased from the authentication once verified.

## License

Apache License 2.0.
