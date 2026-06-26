package fr.voyager3.callcontroller.data

import fr.voyager3.callcontroller.matching.Regle
import fr.voyager3.callcontroller.matching.TypeRegle
import kotlinx.serialization.Serializable

/** Représentation JSON d'un preset communautaire (voir SPEC.md). */
@Serializable
data class PresetDto(
    val id: String,
    val name: String,
    val description: String = "",
    val version: Int = 1,
    val author: String = "",
    val source: String = "",
    val rules: List<RegleDto> = emptyList(),
)

@Serializable
data class RegleDto(
    val type: String,
    val value: String,
    val label: String = "",
) {
    /** Convertit en règle de domaine, ou null si invalide (input non fiable → ignoré). */
    fun versDomaine(presetId: String): Regle? {
        val typeRegle = when (type.lowercase()) {
            "prefix", "prefixe" -> TypeRegle.PREFIXE
            "regex" -> TypeRegle.REGEX
            else -> return null
        }
        if (value.isBlank()) return null
        return Regle(type = typeRegle, valeur = value.trim(), presetId = presetId)
    }
}
