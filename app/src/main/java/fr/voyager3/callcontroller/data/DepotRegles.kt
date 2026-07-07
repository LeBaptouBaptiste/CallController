package fr.voyager3.callcontroller.data

import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.CacheRegles
import fr.voyager3.callcontroller.matching.Regle
import fr.voyager3.callcontroller.matching.TypeRegle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Source de vérité des règles. Persiste en base (Room) ; les mutations se
 * propagent automatiquement à l'UI (via [regles]) et au cache du service de
 * screening (via [maintenirCacheAJour]) — aucun rafraîchissement manuel.
 */
class DepotRegles(
    private val dao: RegleDao,
    private val chargeurPreset: ChargeurPreset,
) {
    val regles: Flow<List<Regle>> =
        dao.observerToutes().map { liste -> liste.map { it.versDomaine() } }

    private val reglesActives: Flow<List<Regle>> =
        dao.observerActives().map { liste -> liste.map { it.versDomaine() } }

    /** Au premier lancement, sème le preset ARCEP par défaut. */
    suspend fun amorcerSiVide() {
        if (dao.compter() == 0) {
            val reglesArcep = chargeurPreset.chargerDepuisAssets(ChargeurPreset.PRESET_ARCEP)
            dao.upsert(reglesArcep.map { RegleEntity.depuisDomaine(it) })
        }
    }

    /**
     * Maintient l'instantané en mémoire lu par le service de screening. À lancer
     * dans une portée longue durée (Application). Ne retourne jamais (collecte continue).
     */
    suspend fun maintenirCacheAJour() {
        reglesActives.collect { CacheRegles.remplacer(it) }
    }

    /**
     * Charge immédiatement le cache du service (lecture unique, bloquante côté
     * appelant). Appelé au démarrage pour garantir que les règles sont prêtes
     * avant le premier appel screené (évite le fail-open sur démarrage à froid).
     */
    suspend fun rafraichirCacheMaintenant() {
        CacheRegles.remplacer(reglesActives.first())
    }

    suspend fun ajouter(valeur: String, type: TypeRegle, action: ActionRegle) {
        val nettoyee = valeur.trim()
        if (nettoyee.isEmpty()) return
        dao.inserer(RegleEntity(type = type, valeur = nettoyee, action = action))
    }

    suspend fun definirActif(id: Long, actif: Boolean) = dao.definirActif(id, actif)

    suspend fun supprimer(id: Long) = dao.supprimer(id)
}
