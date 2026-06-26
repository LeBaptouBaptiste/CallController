package fr.voyager3.callcontroller.matching

/** Type de motif utilisé pour comparer un numéro entrant. */
enum class TypeRegle { PREFIXE, REGEX }

/** Effet d'une règle : bloquer le numéro, ou l'autoriser toujours (liste blanche). */
enum class ActionRegle { BLOQUER, AUTORISER }

/** Décision rendue par le moteur pour un numéro entrant. */
enum class Decision { AUTORISER, REJETER }

/**
 * Règle de filtrage : un motif (préfixe ou regex) associé à une action. La liste
 * blanche ([ActionRegle.AUTORISER]) est prioritaire sur le blocage.
 */
data class Regle(
    val id: Long = 0,
    val type: TypeRegle,
    val valeur: String,
    val action: ActionRegle = ActionRegle.BLOQUER,
    val presetId: String? = null,
    val actif: Boolean = true,
)
