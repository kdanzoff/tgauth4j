package io.github.kdanzoff.tgauth4j.test

import java.time.Instant

/**
 * Contenu d'un `initData` de test, signé par [TelegramTestInitData.signed] comme le fait Telegram.
 *
 * Par défaut : utilisateur `1`, prénom `Test`, signé à l'instant présent.
 */
public class InitDataSpec {

    /** Identifiant Telegram de l'utilisateur. */
    public var userId: Long = 1

    /** Prénom. */
    public var firstName: String = "Test"

    /** Nom de famille. */
    public var lastName: String? = null

    /** Pseudonyme `@…`. */
    public var username: String? = null

    /** Langue. */
    public var languageCode: String? = null

    /** Moment de la signature (`auth_date`). */
    public var authDate: Instant = Instant.now()

    /** Identifiant de la session de la Mini App. */
    public var queryId: String? = "AAHdF6IQAAAAAN0XohDhrOrc"

    /** Paramètre `startapp`. */
    public var startParam: String? = null

    /** Champs supplémentaires, ajoutés tels quels. */
    public val extraFields: MutableMap<String, String> = mutableMapOf()

    /** Méthodes chaînables pour Java. */
    public fun userId(userId: Long): InitDataSpec = apply { this.userId = userId }

    /** Voir [firstName]. */
    public fun firstName(firstName: String): InitDataSpec = apply { this.firstName = firstName }

    /** Voir [username]. */
    public fun username(username: String?): InitDataSpec = apply { this.username = username }

    /** Voir [authDate]. */
    public fun authDate(authDate: Instant): InitDataSpec = apply { this.authDate = authDate }

    /** Voir [startParam]. */
    public fun startParam(startParam: String?): InitDataSpec = apply { this.startParam = startParam }
}
