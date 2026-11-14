package io.github.kdanzoff.tgauth4j.core

import java.time.Instant

/**
 * `initData` vérifié : ce que Telegram a signé à l'ouverture de la Mini App.
 *
 * Données personnelles : [toString] ne montre que l'identifiant de l'utilisateur et [authDate].
 *
 * @property user utilisateur qui a ouvert la Mini App.
 * @property authDate moment de la signature par Telegram.
 * @property queryId identifiant de la session de la Mini App, pour `answerWebAppQuery`, s'il est présent.
 * @property startParam paramètre `startapp` du lien d'ouverture, s'il est présent.
 * @property chatType type de discussion d'où la Mini App a été ouverte, s'il est présent.
 * @property chatInstance identifiant de cette discussion, s'il est présent.
 * @property fields tous les champs reçus, décodés (y compris ceux que la bibliothèque ne lit pas).
 */
public data class TelegramInitData(
    val user: TelegramUser,
    val authDate: Instant,
    val queryId: String? = null,
    val startParam: String? = null,
    val chatType: String? = null,
    val chatInstance: String? = null,
    val fields: Map<String, String> = emptyMap(),
) {
    override fun toString(): String = "TelegramInitData(userId=${user.id}, authDate=$authDate)"
}
