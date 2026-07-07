package fr.voyager3.callcontroller

import android.app.Application
import fr.voyager3.callcontroller.di.AppContainer
import fr.voyager3.callcontroller.matching.CacheReglages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class CallControllerApp : Application() {

    lateinit var container: AppContainer
        private set

    private val porteeApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Chargement initial SYNCHRONE : le système appelle onCreate() AVANT de lier
        // le service de screening. On garantit ainsi que les caches lus dans
        // onScreenCall sont prêts dès le premier appel, même après un démarrage à
        // froid du process — sinon l'appel passait « fail open » pendant le
        // chargement asynchrone. Borné par un timeout de sécurité.
        runBlocking {
            withTimeoutOrNull(DELAI_AMORCAGE_MS) {
                container.depotRegles.amorcerSiVide()
                container.depotRegles.rafraichirCacheMaintenant()
                CacheReglages.bloquerMasques = container.depotReglages.lireBloquerMasques()
            }
        }

        // Maintien réactif pour les changements ultérieurs (ajout de règle, toggle...).
        porteeApp.launch { container.depotRegles.maintenirCacheAJour() }
        porteeApp.launch {
            container.depotReglages.bloquerMasques.collect { CacheReglages.bloquerMasques = it }
        }
    }

    /**
     * Journalise un appel bloqué depuis la portée de l'application (le process
     * survit au cycle de vie court du service de screening).
     */
    fun journaliserAppelBloque(numero: String, motif: String?) {
        porteeApp.launch { container.depotJournal.enregistrer(numero, motif) }
    }

    private companion object {
        const val DELAI_AMORCAGE_MS = 3000L
    }
}
