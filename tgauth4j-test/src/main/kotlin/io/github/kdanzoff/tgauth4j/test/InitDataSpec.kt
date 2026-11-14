package io.github.kdanzoff.tgauth4j.test

import java.time.Instant

/**
 * Content of a test `initData`, signed by [TelegramTestInitData.signed] the way Telegram does.
 *
 * Defaults: user `1`, first name `Test`, signed now.
 */
public class InitDataSpec {

    /** Telegram id of the user. */
    public var userId: Long = 1

    /** First name. */
    public var firstName: String = "Test"

    /** Last name. */
    public var lastName: String? = null

    /** `@…` handle. */
    public var username: String? = null

    /** Language. */
    public var languageCode: String? = null

    /** Signing time (`auth_date`). */
    public var authDate: Instant = Instant.now()

    /** Mini App session id. */
    public var queryId: String? = "AAHdF6IQAAAAAN0XohDhrOrc"

    /** `startapp` parameter. */
    public var startParam: String? = null

    /** Extra fields, added as is. */
    public val extraFields: MutableMap<String, String> = mutableMapOf()

    /** Chainable methods for Java. */
    public fun userId(userId: Long): InitDataSpec = apply { this.userId = userId }

    /** See [firstName]. */
    public fun firstName(firstName: String): InitDataSpec = apply { this.firstName = firstName }

    /** See [username]. */
    public fun username(username: String?): InitDataSpec = apply { this.username = username }

    /** See [authDate]. */
    public fun authDate(authDate: Instant): InitDataSpec = apply { this.authDate = authDate }

    /** See [startParam]. */
    public fun startParam(startParam: String?): InitDataSpec = apply { this.startParam = startParam }
}
