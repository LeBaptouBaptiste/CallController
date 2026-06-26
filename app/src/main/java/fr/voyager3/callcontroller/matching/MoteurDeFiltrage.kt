package fr.voyager3.callcontroller.matching

/** Résultat d'une évaluation : la décision et, le cas échéant, le motif qui l'a déclenchée. */
data class ResultatEvaluation(val decision: Decision, val motif: String? = null)

/**
 * Moteur de décision : compile une fois les règles actives puis évalue chaque
 * numéro entrant. La liste blanche est prioritaire sur le blocage.
 *
 * Logique pure (aucune dépendance Android) afin d'être couverte par des tests
 * unitaires JVM — c'est le cœur métier.
 */
class MoteurDeFiltrage(regles: List<Regle>) {

    /** Critère compilé : conserve la valeur source (pour le motif) et la fonction de comparaison. */
    private class Critere(val source: String, val correspond: (String) -> Boolean)

    private val autorises: List<Critere> = compiler(regles, ActionRegle.AUTORISER)
    private val bloquants: List<Critere> = compiler(regles, ActionRegle.BLOQUER)

    fun evaluerDetail(numeroBrut: String): ResultatEvaluation {
        val numero = NormalisateurNumero.normaliser(numeroBrut)
        if (numero.isEmpty()) return ResultatEvaluation(Decision.AUTORISER)

        autorises.firstOrNull { it.correspond(numero) }
            ?.let { return ResultatEvaluation(Decision.AUTORISER, it.source) }
        bloquants.firstOrNull { it.correspond(numero) }
            ?.let { return ResultatEvaluation(Decision.REJETER, it.source) }
        return ResultatEvaluation(Decision.AUTORISER)
    }

    fun evaluer(numeroBrut: String): Decision = evaluerDetail(numeroBrut).decision

    fun doitBloquer(numeroBrut: String): Boolean = evaluer(numeroBrut) == Decision.REJETER

    private fun compiler(regles: List<Regle>, action: ActionRegle): List<Critere> =
        regles.asSequence()
            .filter { it.actif && it.action == action }
            .mapNotNull { critere(it) }
            .toList()

    private fun critere(regle: Regle): Critere? = when (regle.type) {
        TypeRegle.PREFIXE -> regle.valeur.filter(Char::isDigit)
            .takeIf { it.isNotEmpty() }
            ?.let { prefixe -> Critere(regle.valeur) { numero -> numero.startsWith(prefixe) } }

        // Les regex communautaires sont compilées une seule fois. La protection
        // anti-ReDoS (bornage du temps d'évaluation) reste à ajouter avant
        // d'autoriser des presets regex arbitraires — voir SPEC.md.
        TypeRegle.REGEX -> compilerRegex(regle.valeur)
            ?.let { regex -> Critere(regle.valeur) { numero -> regex.matches(numero) } }
    }

    /** Motif invalide (input non fiable) → règle ignorée plutôt que crash. */
    private fun compilerRegex(motif: String): Regex? =
        try {
            Regex(motif)
        } catch (_: IllegalArgumentException) {
            null
        }
}
