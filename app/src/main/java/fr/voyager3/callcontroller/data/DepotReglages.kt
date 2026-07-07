package fr.voyager3.callcontroller.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// corruptionHandler : reconstruit le fichier si son contenu est corrompu, au lieu
// de laisser DataStore lever une CorruptionException à chaque lecture.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "reglages",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

/** Réglages persistés de l'application (DataStore Preferences). */
class DepotReglages(private val context: Context) {

    private val cleBloquerMasques = booleanPreferencesKey("bloquer_masques")

    // .catch : une erreur d'I/O ne doit jamais faire remonter d'exception au
    // collecteur (sinon crash du process). On retombe sur les valeurs par défaut.
    val bloquerMasques: Flow<Boolean> = context.dataStore.data
        .catch { erreur ->
            if (erreur is IOException) emit(emptyPreferences()) else throw erreur
        }
        .map { prefs -> prefs[cleBloquerMasques] ?: false }

    suspend fun definirBloquerMasques(valeur: Boolean) {
        context.dataStore.edit { prefs -> prefs[cleBloquerMasques] = valeur }
    }
}
