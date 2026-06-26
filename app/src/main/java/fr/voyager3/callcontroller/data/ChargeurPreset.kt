package fr.voyager3.callcontroller.data

import android.content.Context
import fr.voyager3.callcontroller.matching.Regle
import kotlinx.serialization.json.Json

/** Lit et valide les presets embarqués dans les assets de l'app. */
class ChargeurPreset(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Charge un preset depuis `assets/presets/<fichier>` et retourne ses règles
     * valides. Tout input est validé : un type inconnu ou une valeur vide est ignoré.
     */
    fun chargerDepuisAssets(fichier: String): List<Regle> {
        val contenu = context.assets.open("presets/$fichier")
            .bufferedReader()
            .use { it.readText() }
        val preset = json.decodeFromString<PresetDto>(contenu)
        return preset.rules.mapNotNull { it.versDomaine(preset.id) }
    }

    companion object {
        const val PRESET_ARCEP = "fr-demarchage-arcep.json"
    }
}
