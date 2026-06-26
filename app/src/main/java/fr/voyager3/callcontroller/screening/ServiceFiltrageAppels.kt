package fr.voyager3.callcontroller.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import fr.voyager3.callcontroller.CallControllerApp
import fr.voyager3.callcontroller.matching.CacheRegles
import fr.voyager3.callcontroller.matching.CacheReglages
import fr.voyager3.callcontroller.matching.Decision
import fr.voyager3.callcontroller.matching.ResultatEvaluation

/**
 * Point d'entrée système : appelé avant la sonnerie pour chaque appel entrant.
 *
 * Hot path : décision rapide, en mémoire, hors-ligne. On lit les instantanés
 * pré-calculés ([CacheRegles], [CacheReglages]) — aucune I/O ni accès base ici.
 * La journalisation d'un blocage est déléguée (asynchrone) à l'application.
 *
 * Comportement « fail open » : si le cache n'est pas encore amorcé (ex. appel
 * juste après un démarrage à froid), l'appel est autorisé. Voir SPEC.md.
 */
class ServiceFiltrageAppels : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val numero = callDetails.handle?.schemeSpecificPart
        val resultat = when {
            CacheReglages.bloquerMasques && estMasque(callDetails, numero) ->
                ResultatEvaluation(Decision.REJETER, MOTIF_MASQUE)

            numero != null -> CacheRegles.moteur.evaluerDetail(numero)
            else -> ResultatEvaluation(Decision.AUTORISER)
        }
        val doitBloquer = resultat.decision == Decision.REJETER

        val reponse = CallResponse.Builder()
            .setDisallowCall(doitBloquer)
            .setRejectCall(doitBloquer)
            .setSkipNotification(doitBloquer)
            .build()
        respondToCall(callDetails, reponse)

        if (doitBloquer) {
            val libelle = numero ?: MOTIF_MASQUE
            (applicationContext as CallControllerApp).journaliserAppelBloque(libelle, resultat.motif)
        }
    }

    /** Appel sans identité affichée : numéro volontairement caché (privé) ou absent (inconnu). */
    private fun estMasque(details: Call.Details, numero: String?): Boolean =
        details.handlePresentation == TelecomManager.PRESENTATION_RESTRICTED || numero.isNullOrBlank()

    private companion object {
        const val MOTIF_MASQUE = "Numéro masqué"
    }
}
