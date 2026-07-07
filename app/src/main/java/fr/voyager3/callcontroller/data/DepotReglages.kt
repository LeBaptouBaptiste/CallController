package fr.voyager3.callcontroller.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reglages")

/** Réglages persistés de l'application (DataStore Preferences). */
class DepotReglages(private val context: Context) {

    private val cleBloquerMasques = booleanPreferencesKey("bloquer_masques")

    val bloquerMasques: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[cleBloquerMasques] ?: false }

    suspend fun definirBloquerMasques(valeur: Boolean) {
        context.dataStore.edit { prefs -> prefs[cleBloquerMasques] = valeur }
    }

    /** Lecture unique du réglage (pour l'amorçage synchrone du cache au démarrage). */
    suspend fun lireBloquerMasques(): Boolean = bloquerMasques.first()
}
