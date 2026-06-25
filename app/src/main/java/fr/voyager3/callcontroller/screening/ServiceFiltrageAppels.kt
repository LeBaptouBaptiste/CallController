package fr.voyager3.callcontroller.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import fr.voyager3.callcontroller.CallControllerApp
import fr.voyager3.callcontroller.matching.CacheRegles
import fr.voyager3.callcontroller.matching.Decision
import fr.voyager3.callcontroller.matching.ResultatEvaluation

/**
 * Point d'entrée système : appelé avant la sonnerie pour chaque appel entrant.
 *
 * Hot path : décision rapide, en mémoire, hors-ligne. On lit l'instantané
 * pré-calculé dans [CacheRegles] — aucune I/O ni accès base ici. La
 * journalisation d'un blocage est déléguée (asynchrone) à l'application.
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
        val resultat = if (numero != null) {
            CacheRegles.moteur.evaluerDetail(numero)
        } else {
            ResultatEvaluation(Decision.AUTORISER)
        }
        val doitBloquer = resultat.decision == Decision.REJETER

        val reponse = CallResponse.Builder()
            .setDisallowCall(doitBloquer)
            .setRejectCall(doitBloquer)
            .setSkipNotification(doitBloquer)
            .build()
        respondToCall(callDetails, reponse)

        if (doitBloquer && numero != null) {
            (applicationContext as CallControllerApp).journaliserAppelBloque(numero, resultat.motif)
        }
    }
}
