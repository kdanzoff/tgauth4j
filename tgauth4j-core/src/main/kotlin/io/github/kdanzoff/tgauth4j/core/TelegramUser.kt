package io.github.kdanzoff.tgauth4j.core

/**
 * Utilisateur Telegram, lu dans le champ `user` d'`initData`.
 *
 * Données personnelles : [toString] ne montre que [id].
 *
 * @property id identifiant Telegram, stable.
 * @property firstName prénom.
 * @property lastName nom de famille, s'il est renseigné.
 * @property username pseudonyme `@…`, facultatif et modifiable : ne pas s'en servir comme identifiant.
 * @property languageCode langue de l'application Telegram (IETF), si connue.
 * @property isPremium abonné Telegram Premium.
 * @property photoUrl adresse de la photo de profil, si Telegram la fournit.
 */
public data class TelegramUser(
    val id: Long,
    val firstName: String,
    val lastName: String? = null,
    val username: String? = null,
    val languageCode: String? = null,
    val isPremium: Boolean = false,
    val photoUrl: String? = null,
) {
    override fun toString(): String = "TelegramUser(id=$id)"
}
