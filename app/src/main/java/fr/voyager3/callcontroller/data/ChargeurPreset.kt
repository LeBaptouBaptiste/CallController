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
        return parser(contenu)
    }

    /**
     * Parse et valide un preset (JSON) en règles de domaine. Utilisé pour les
     * presets embarqués comme pour ceux téléchargés — tout input est borné.
     */
    fun parser(contenu: String): List<Regle> {
        val preset = json.decodeFromString<PresetDto>(contenu)
        return preset.rules.asSequence()
            .take(MAX_REGLES)
            .mapNotNull { it.versDomaine(preset.id) }
            .toList()
    }

    companion object {
        const val PRESET_ARCEP = "fr-demarchage-arcep.json"
        private const val MAX_REGLES = 10_000
    }
}
