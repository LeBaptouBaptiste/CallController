package fr.voyager3.callcontroller

import android.app.Application
import android.util.Log
import fr.voyager3.callcontroller.di.AppContainer
import fr.voyager3.callcontroller.matching.CacheRegles
import fr.voyager3.callcontroller.matching.CacheReglages
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CallControllerApp : Application() {

    lateinit var container: AppContainer
        private set

    private val porteeApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Signaux d'amorçage attendus par le service de screening avant de décider :
    // ferme la fenêtre « fail open » du démarrage à froid, sans bloquer de thread.
    private val reglesPretes = CompletableDeferred<Unit>()
    private val reglagesPrets = CompletableDeferred<Unit>()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Sème le preset par défaut (une fois), puis alimente en continu le cache
        // du service. Le semis se termine toujours (aucun timeout annulable).
        porteeApp.launch {
            runCatching { container.depotRegles.amorcerSiVide() }
                .onFailure { Log.e(TAG, "Semis du preset par défaut impossible", it) }

            container.depotRegles.reglesActives
                .catch { erreur ->
                    Log.e(TAG, "Lecture des règles impossible", erreur)
                    emit(emptyList())
                }
                .collect { regles ->
                    CacheRegles.remplacer(regles)
                    reglesPretes.complete(Unit)
                }
        }

        porteeApp.launch {
            container.depotReglages.bloquerMasques.collect { actif ->
                CacheReglages.bloquerMasques = actif
                reglagesPrets.complete(Unit)
            }
        }
    }

    /** Suspend jusqu'à ce que les caches lus par le service soient amorcés. */
    suspend fun attendrePret() {
        reglesPretes.await()
        reglagesPrets.await()
    }

    /**
     * Journalise un appel bloqué depuis la portée de l'application (le process
     * survit au cycle de vie court du service de screening).
     */
    fun journaliserAppelBloque(numero: String, motif: String?) {
        porteeApp.launch { container.depotJournal.enregistrer(numero, motif) }
    }

    private companion object {
        const val TAG = "CallController"
    }
}
