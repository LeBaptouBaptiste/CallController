package fr.voyager3.callcontroller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.voyager3.callcontroller.data.AppelBloque
import fr.voyager3.callcontroller.data.DepotJournal
import fr.voyager3.callcontroller.data.DepotParametres
import fr.voyager3.callcontroller.data.DepotPresets
import fr.voyager3.callcontroller.data.DepotRegles
import fr.voyager3.callcontroller.data.EntreeCatalogue
import fr.voyager3.callcontroller.di.AppContainer
import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.CacheRegles
import fr.voyager3.callcontroller.matching.Regle
import fr.voyager3.callcontroller.matching.ResultatEvaluation
import fr.voyager3.callcontroller.matching.TypeRegle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** État et actions de l'écran principal. Garde les composables sans logique métier. */
class AppViewModel(
    private val depotRegles: DepotRegles,
    private val depotJournal: DepotJournal,
    private val depotParametres: DepotParametres,
    private val depotPresets: DepotPresets,
) : ViewModel() {

    val regles: Flow<List<Regle>> = depotRegles.regles
    val journal: Flow<List<AppelBloque>> = depotJournal.appels
    val bloquerMasques: Flow<Boolean> = depotParametres.bloquerMasques
    val abonnements: Flow<Set<String>> = depotPresets.abonnements

    private val _etatCatalogue = MutableStateFlow<EtatCatalogue>(EtatCatalogue.Chargement)
    val etatCatalogue: StateFlow<EtatCatalogue> = _etatCatalogue.asStateFlow()

    private val _erreurAction = MutableStateFlow<String?>(null)
    val erreurAction: StateFlow<String?> = _erreurAction.asStateFlow()

    init {
        rafraichirCatalogue()
    }

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

    fun definirBloquerMasques(valeur: Boolean) {
        viewModelScope.launch { depotParametres.definirBloquerMasques(valeur) }
    }

    fun tester(numero: String): ResultatEvaluation = CacheRegles.moteur.evaluerDetail(numero)

    fun rafraichirCatalogue() {
        viewModelScope.launch {
            _etatCatalogue.value = EtatCatalogue.Chargement
            _etatCatalogue.value = runCatching { depotPresets.chargerCatalogue() }.fold(
                onSuccess = { EtatCatalogue.Charge(it) },
                onFailure = { EtatCatalogue.Erreur(it.message ?: "Catalogue indisponible") },
            )
        }
    }

    fun sabonner(entree: EntreeCatalogue) {
        viewModelScope.launch {
            runCatching { depotPresets.appliquer(entree) }
                .onFailure { _erreurAction.value = "Échec : ${it.message ?: "réseau indisponible"}" }
        }
    }

    fun seDesabonner(presetId: String) {
        viewModelScope.launch { depotPresets.seDesabonner(presetId) }
    }

    fun effacerErreur() {
        _erreurAction.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppViewModel(
                    container.depotRegles,
                    container.depotJournal,
                    container.depotParametres,
                    container.depotPresets,
                )
            }
        }
    }
}
