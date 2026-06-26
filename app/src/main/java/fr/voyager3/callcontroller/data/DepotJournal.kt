package fr.voyager3.callcontroller.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Dépôt du journal des appels bloqués. */
class DepotJournal(private val dao: AppelBloqueDao) {

    val appels: Flow<List<AppelBloque>> =
        dao.observerRecents().map { liste -> liste.map { it.versDomaine() } }

    suspend fun enregistrer(numero: String, motif: String?) {
        dao.inserer(
            AppelBloqueEntity(
                numero = numero,
                horodatage = System.currentTimeMillis(),
                motif = motif,
            ),
        )
    }

    suspend fun vider() = dao.vider()
}
