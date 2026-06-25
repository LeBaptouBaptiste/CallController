package fr.voyager3.callcontroller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.voyager3.callcontroller.data.AppelBloque
import fr.voyager3.callcontroller.data.DepotJournal
import fr.voyager3.callcontroller.data.DepotRegles
import fr.voyager3.callcontroller.di.AppContainer
import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.CacheRegles
import fr.voyager3.callcontroller.matching.Regle
import fr.voyager3.callcontroller.matching.ResultatEvaluation
import fr.voyager3.callcontroller.matching.TypeRegle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** État et actions de l'écran principal. Garde les composables sans logique métier. */
class AppViewModel(
    private val depotRegles: DepotRegles,
    private val depotJournal: DepotJournal,
) : ViewModel() {

    val regles: Flow<List<Regle>> = depotRegles.regles
    val journal: Flow<List<AppelBloque>> = depotJournal.appels

    fun ajouter(valeur: String, type: TypeRegle, action: ActionRegle) {
        viewModelScope.launch { depotRegles.ajouter(valeur, type, action) }
    }

    fun basculer(regle: Regle) {
        viewModelScope.launch { depotRegles.definirActif(regle.id, !regle.actif) }
    }

    fun supprimer(regle: Regle) {
        viewModelScope.launch { depotRegles.supprimer(regle.id) }
    }

    fun viderJournal() {
        viewModelScope.launch { depotJournal.vider() }
    }

    /** Évalue un numéro contre les règles actives (même moteur que le service de screening). */
    fun tester(numero: String): ResultatEvaluation = CacheRegles.moteur.evaluerDetail(numero)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AppViewModel(container.depotRegles, container.depotJournal) }
        }
    }
}
