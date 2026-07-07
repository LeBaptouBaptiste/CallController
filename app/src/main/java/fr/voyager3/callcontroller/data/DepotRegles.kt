package fr.voyager3.callcontroller.data

import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.Regle
import fr.voyager3.callcontroller.matching.TypeRegle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Source de vérité des règles (accès aux données, Room). L'alimentation du cache
 * du service de screening et la logique de démarrage sont orchestrées par
 * l'Application, qui consomme [reglesActives].
 */
class DepotRegles(
    private val dao: RegleDao,
    private val chargeurPreset: ChargeurPreset,
) {
    /** Toutes les règles, pour l'UI. */
    val regles: Flow<List<Regle>> =
        dao.observerToutes().map { liste -> liste.map { it.versDomaine() } }

    /** Règles actives, consommées pour alimenter le cache du service de screening. */
    val reglesActives: Flow<List<Regle>> =
        dao.observerActives().map { liste -> liste.map { it.versDomaine() } }

    /** Au premier lancement, sème le preset ARCEP par défaut. */
    suspend fun amorcerSiVide() {
        if (dao.compter() == 0) {
            val reglesArcep = chargeurPreset.chargerDepuisAssets(ChargeurPreset.PRESET_ARCEP)
            dao.upsert(reglesArcep.map { RegleEntity.depuisDomaine(it) })
        }
    }

    suspend fun ajouter(valeur: String, type: TypeRegle, action: ActionRegle) {
        val nettoyee = valeur.trim()
        if (nettoyee.isEmpty()) return
        dao.inserer(RegleEntity(type = type, valeur = nettoyee, action = action))
    }

    suspend fun definirActif(id: Long, actif: Boolean) = dao.definirActif(id, actif)

    suspend fun supprimer(id: Long) = dao.supprimer(id)
}
