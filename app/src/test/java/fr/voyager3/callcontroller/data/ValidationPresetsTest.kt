package fr.voyager3.callcontroller.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Valide les presets communautaires du dépôt (dossier `/presets`) à chaque CI.
 *
 * Utilise les **mêmes modèles que l'app** (`CatalogueDto`, `PresetDto`) : zéro
 * duplication de schéma, et on rejette exactement ce que l'app rejetterait
 * (JSON mal formé, champ requis manquant, type inconnu, url non HTTPS…). Un
 * preset cassé fait échouer la CI avant d'atteindre `main`.
 */
class ValidationPresetsTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val dossierPresets: File = trouverDossierPresets()

    @Test
    fun `le catalogue est un JSON valide et conforme au schema`() {
        val catalogue = lireCatalogue()
        assertTrue("Le catalogue ne référence aucun preset", catalogue.presets.isNotEmpty())
        catalogue.presets.forEach { entree ->
            assertNotNull(
                "Entrée de catalogue invalide (id vide ou url non HTTPS) : " +
                    "id='${entree.id}', url='${entree.url}'",
                entree.versDomaine(),
            )
        }
    }

    @Test
    fun `chaque preset du catalogue existe, correspond et a au moins une regle valide`() {
        lireCatalogue().presets.forEach { entree ->
            val nomFichier = entree.url.substringAfterLast('/')
            val fichier = File(dossierPresets, nomFichier)
            assertTrue(
                "Preset référencé introuvable : $nomFichier (entrée '${entree.id}')",
                fichier.isFile,
            )
            val preset = json.decodeFromString<PresetDto>(fichier.readText())
            assertEquals(
                "L'id du preset ($nomFichier) ne correspond pas à l'entrée de catalogue",
                entree.id,
                preset.id,
            )
            assertTrue(
                "Le preset $nomFichier n'a aucune règle valide (type/valeur)",
                preset.rules.mapNotNull { it.versDomaine(preset.id) }.isNotEmpty(),
            )
        }
    }

    @Test
    fun `tous les fichiers preset du dossier sont des JSON conformes`() {
        fichiersPresets().forEach { fichier ->
            // Doit se parser sans exception : syntaxe correcte + champs requis (id, name).
            json.decodeFromString<PresetDto>(fichier.readText())
        }
    }

    private fun lireCatalogue(): CatalogueDto {
        val fichier = File(dossierPresets, "catalogue.json")
        assertTrue("catalogue.json introuvable dans ${dossierPresets.path}", fichier.isFile)
        return json.decodeFromString(fichier.readText())
    }

    private fun fichiersPresets(): List<File> {
        val fichiers = dossierPresets.listFiles { f ->
            f.isFile && f.extension == "json" && f.name != "catalogue.json"
        }
        return fichiers?.sortedBy { it.name }.orEmpty()
    }

    private fun trouverDossierPresets(): File {
        // Le working dir des tests Gradle est le dossier du module (app/). On remonte
        // jusqu'à trouver le dossier "presets" contenant le catalogue (racine du repo).
        var courant: File? = File("").absoluteFile
        while (courant != null) {
            val candidat = File(courant, "presets")
            if (File(candidat, "catalogue.json").isFile) return candidat
            courant = courant.parentFile
        }
        error("Dossier 'presets/' introuvable en remontant depuis ${File("").absolutePath}")
    }
}
