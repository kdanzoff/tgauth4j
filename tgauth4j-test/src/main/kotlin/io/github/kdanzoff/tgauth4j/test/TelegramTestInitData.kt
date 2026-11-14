package io.github.kdanzoff.tgauth4j.test

import org.springframework.test.web.servlet.request.RequestPostProcessor
import tools.jackson.databind.json.JsonMapper
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Test `initData`, signed with a test bot token using the algorithm from the Telegram documentation.
 *
 * ```
 * mockMvc.get("/api/me") { with(TelegramTestInitData.telegramInitData(BOT_TOKEN) { userId = 42 }) }
 * ```
 */
public object TelegramTestInitData {

    private val json = JsonMapper.builder().build()

    /** `initData` described by [spec], signed with [botToken]. */
    @JvmStatic
    @JvmOverloads
    public fun signed(botToken: String, spec: InitDataSpec.() -> Unit = {}): String = sign(botToken, InitDataSpec().apply(spec))

    /** For Java: `initData` described by [spec], signed with [botToken]. */
    @JvmStatic
    public fun signed(botToken: String, spec: InitDataSpec): String = sign(botToken, spec)

    /** `Authorization: tma <initData>` header of a MockMvc request (requires the `spring-test` dependency). */
    @JvmStatic
    @JvmOverloads
    public fun telegramInitData(botToken: String, spec: InitDataSpec.() -> Unit = {}): RequestPostProcessor {
        val initData = signed(botToken, spec)
        return RequestPostProcessor { request -> request.apply { addHeader(AUTHORIZATION, "$SCHEME$initData") } }
    }

    private fun sign(botToken: String, spec: InitDataSpec): String {
        val fields = fieldsOf(spec)
        val secret = hmac(WEB_APP_DATA.toByteArray(), botToken.toByteArray())
        val check = fields.toSortedMap().entries.joinToString("\n") { "${it.key}=${it.value}" }
        val hash = hmac(secret, check.toByteArray()).joinToString("") { "%02x".format(it) }
        return (fields + (HASH to hash)).entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, Charsets.UTF_8)}" }
    }

    private fun fieldsOf(spec: InitDataSpec): Map<String, String> = buildMap {
        put("auth_date", spec.authDate.epochSecond.toString())
        put("user", userJson(spec))
        spec.queryId?.let { put("query_id", it) }
        spec.startParam?.let { put("start_param", it) }
        putAll(spec.extraFields)
    }

    private fun userJson(spec: InitDataSpec): String = json.writeValueAsString(
        buildMap {
            put("id", spec.userId)
            put("first_name", spec.firstName)
            spec.lastName?.let { put("last_name", it) }
            spec.username?.let { put("username", it) }
            spec.languageCode?.let { put("language_code", it) }
        },
    )

    private fun hmac(key: ByteArray, message: ByteArray): ByteArray =
        Mac.getInstance(HMAC_SHA256).apply { init(SecretKeySpec(key, HMAC_SHA256)) }.doFinal(message)

    private const val WEB_APP_DATA = "WebAppData"
    private const val HMAC_SHA256 = "HmacSHA256"
    private const val HASH = "hash"
    private const val AUTHORIZATION = "Authorization"
    private const val SCHEME = "tma "
}
