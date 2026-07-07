package fr.voyager3.callcontroller.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Synchronisation des presets communautaires (Option A : catalogue intégré).
 * Un preset auquel on est « abonné » = un preset dont des règles existent en base
 * (identifiées par leur presetId).
 */
class DepotPresets(
    private val source: SourcePresets,
    private val chargeurPreset: ChargeurPreset,
    private val regleDao: RegleDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** presetId actuellement présents en base (= abonnements). */
    val abonnements: Flow<Set<String>> =
        regleDao.observerPresetIds().map { liste -> liste.toSet() }

    /** Télécharge et valide le catalogue distant. Lève en cas d'erreur (géré par l'appelant). */
    suspend fun chargerCatalogue(): List<EntreeCatalogue> {
        val contenu = source.telecharger(URL_CATALOGUE)
        val catalogue = json.decodeFromString<CatalogueDto>(contenu)
        return catalogue.presets.mapNotNull { it.versDomaine() }
    }

    /**
     * Télécharge un preset et remplace ses règles en base (abonnement ou mise à
     * jour). Les règles portent l'id de l'entrée de catalogue pour rester cohérentes.
     */
    suspend fun appliquer(entree: EntreeCatalogue) {
        val contenu = source.telecharger(entree.url)
        val regles = chargeurPreset.parser(contenu).map { it.copy(presetId = entree.id) }
        regleDao.supprimerPreset(entree.id)
        regleDao.upsert(regles.map { RegleEntity.depuisDomaine(it) })
    }

    suspend fun seDesabonner(presetId: String) {
        regleDao.supprimerPreset(presetId)
    }

    companion object {
        // Catalogue hébergé sur la branche main du repo. Pour tester avant merge,
        // pointer temporairement vers la branche où catalogue.json est poussé.
        const val URL_CATALOGUE =
            "https://raw.githubusercontent.com/LeBaptouBaptiste/CallController/main/presets/catalogue.json"
    }
}

