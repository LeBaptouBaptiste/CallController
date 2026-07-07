package fr.voyager3.callcontroller.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import fr.voyager3.callcontroller.CallControllerApp
import fr.voyager3.callcontroller.matching.CacheParametres
import fr.voyager3.callcontroller.matching.CacheRegles
import fr.voyager3.callcontroller.matching.Decision
import fr.voyager3.callcontroller.matching.ResultatEvaluation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Point d'entrée système : appelé avant la sonnerie pour chaque appel entrant.
 *
 * La réponse est **asynchrone** (respondToCall peut être appelé après le retour
 * de onScreenCall). On attend que les caches soient amorcés — borné par
 * [DELAI_PRET_MS] pour rester sous le budget système — puis on décide en mémoire,
 * sans I/O ni blocage de thread. Attendre l'amorçage ferme la fenêtre « fail
 * open » du démarrage à froid.
 */
class ServiceFiltrageAppels : CallScreeningService() {

    private val portee = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onScreenCall(callDetails: Call.Details) {
        val app = applicationContext as CallControllerApp
        portee.launch {
            withTimeoutOrNull(DELAI_PRET_MS) { app.attendrePret() }
            repondre(app, callDetails)
        }
    }

    private fun repondre(app: CallControllerApp, callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val numero = callDetails.handle?.schemeSpecificPart
        val masque = estMasque(callDetails, numero)
        val resultat = when {
            CacheParametres.bloquerMasques && masque ->
                ResultatEvaluation(Decision.REJETER, MOTIF_MASQUE)

            !numero.isNullOrBlank() -> CacheRegles.moteur.evaluerDetail(numero)
            else -> ResultatEvaluation(Decision.AUTORISER)
        }
        val doitBloquer = resultat.decision == Decision.REJETER

        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(doitBloquer)
                .setRejectCall(doitBloquer)
                .setSkipNotification(doitBloquer)
                .build(),
        )

        if (doitBloquer) {
            // Ne jamais journaliser un numéro que l'appelant a volontairement masqué.
            val libelle = if (masque) MOTIF_MASQUE else numero.orEmpty()
            app.journaliserAppelBloque(libelle, resultat.motif)
        }
    }

    /** Appel sans identité affichée : numéro volontairement caché (privé) ou absent (inconnu). */
    private fun estMasque(details: Call.Details, numero: String?): Boolean =
        details.handlePresentation == TelecomManager.PRESENTATION_RESTRICTED || numero.isNullOrBlank()

    override fun onDestroy() {
        portee.cancel()
        super.onDestroy()
    }

    private companion object {
        const val MOTIF_MASQUE = "Numéro masqué"
        const val DELAI_PRET_MS = 2500L
    }
}
