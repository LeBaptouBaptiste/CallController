package fr.voyager3.callcontroller

import android.app.Application
import fr.voyager3.callcontroller.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallControllerApp : Application() {

    lateinit var container: AppContainer
        private set

    private val porteeApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Sème le preset par défaut, puis garde en continu le cache du service à jour.
        porteeApp.launch { container.depotRegles.amorcerSiVide() }
        porteeApp.launch { container.depotRegles.maintenirCacheAJour() }
    }

    /**
     * Journalise un appel bloqué depuis la portée de l'application (le process
     * survit au cycle de vie court du service de screening).
     */
    fun journaliserAppelBloque(numero: String, motif: String?) {
        porteeApp.launch { container.depotJournal.enregistrer(numero, motif) }
    }
}
