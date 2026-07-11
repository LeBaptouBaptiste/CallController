package fr.voyager3.callcontroller.matching

import com.google.re2j.Pattern
import com.google.re2j.PatternSyntaxException

/** Résultat d'une évaluation : la décision et, le cas échéant, le motif qui l'a déclenchée. */
data class ResultatEvaluation(val decision: Decision, val motif: String? = null)

/**
 * Moteur de décision : compile une fois les règles actives puis évalue chaque
 * numéro entrant. La liste blanche est prioritaire sur le blocage.
 *
 * Logique pure (aucune dépendance Android) afin d'être couverte par des tests
 * unitaires JVM — c'est le cœur métier.
 *
 * Sécurité (anti-ReDoS) : les regex proviennent de presets communautaires, donc
 * d'un input non fiable. On les évalue avec RE2 ([com.google.re2j.Pattern]), un
 * moteur à temps d'exécution linéaire où le backtracking catastrophique est
 * impossible par conception. En complément, le numéro évalué est borné en
 * longueur ([MAX_LONGUEUR_NUMERO]) : au-delà ce n'est pas un vrai numéro, et le
 * plafond retire tout levier d'explosion résiduel dans le hot path de screening.
 */
class MoteurDeFiltrage(regles: List<Regle>) {

    /** Critère compilé : conserve la valeur source (pour le motif) et la fonction de comparaison. */
    private class Critere(val source: String, val correspond: (String) -> Boolean)

    private val autorises: List<Critere> = compiler(regles, ActionRegle.AUTORISER)
    private val bloquants: List<Critere> = compiler(regles, ActionRegle.BLOQUER)

    fun evaluerDetail(numeroBrut: String): ResultatEvaluation {
        val numero = NormalisateurNumero.normaliser(numeroBrut).take(MAX_LONGUEUR_NUMERO)
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

        // RE2 (temps linéaire) : les regex communautaires ne peuvent pas provoquer
        // de ReDoS. Compilées une seule fois ici, jamais à chaque appel.
        TypeRegle.REGEX -> compilerMotif(regle.valeur)
            ?.let { motif -> Critere(regle.valeur) { numero -> motif.matcher(numero).matches() } }
    }

    /**
     * Motif invalide — syntaxe erronée ou construction non supportée par RE2
     * (backreferences, lookaround) — ignoré plutôt que de faire planter le moteur.
     */
    private fun compilerMotif(motif: String): Pattern? =
        try {
            Pattern.compile(motif)
        } catch (_: PatternSyntaxException) {
            null
        }

    private companion object {
        // E.164 borne un numéro international à 15 chiffres ; on garde une marge.
        // Au-delà ce n'est pas un numéro légitime : on tronque pour borner le coût.
        const val MAX_LONGUEUR_NUMERO = 20
    }
}
