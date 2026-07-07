package fr.voyager3.callcontroller.data

import kotlinx.serialization.Serializable

/** Représentation JSON du catalogue de presets communautaires (hébergé sur GitHub). */
@Serializable
data class CatalogueDto(
    val version: Int = 1,
    val presets: List<EntreeCatalogueDto> = emptyList(),
)

@Serializable
data class EntreeCatalogueDto(
    val id: String,
    val name: String = "",
    val description: String = "",
    val version: Int = 1,
    val url: String = "",
) {
    /** Convertit en entrée de domaine, ou null si invalide (input non fiable → ignoré). */
    fun versDomaine(): EntreeCatalogue? {
        if (id.isBlank() || !url.startsWith("https://")) return null
        return EntreeCatalogue(
            id = id,
            nom = name.ifBlank { id },
            description = description,
            version = version,
            url = url,
        )
    }
}

/** Entrée de catalogue validée. */
data class EntreeCatalogue(
    val id: String,
    val nom: String,
    val description: String,
    val version: Int,
    val url: String,
)
