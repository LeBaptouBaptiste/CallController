package fr.voyager3.callcontroller.matching

/**
 * Met un numéro entrant sous forme canonique avant comparaison.
 *
 * MVP volontairement simple (chiffres + indicatif France). Pour une
 * normalisation robuste à l'international, on pourra introduire libphonenumber
 * plus tard — YAGNI tant que la cible est la France.
 */
object NormalisateurNumero {

    private const val INDICATIF_FRANCE = "33"

    /**
     * Retourne le numéro en format national français (`0XXXXXXXXX`) quand c'est
     * possible, sinon la suite de chiffres. Les caractères de mise en forme
     * (espaces, points, tirets, parenthèses, `+`) sont retirés.
     */
    fun normaliser(numeroBrut: String): String {
        val brut = numeroBrut.trim()
        var chiffres = brut.filter { it.isDigit() }
        if (chiffres.isEmpty()) return ""

        val estInternational = brut.startsWith("+") || chiffres.startsWith("00")
        if (chiffres.startsWith("00")) chiffres = chiffres.drop(2)

        if (estInternational && chiffres.startsWith(INDICATIF_FRANCE)) {
            chiffres = "0" + chiffres.removePrefix(INDICATIF_FRANCE)
        }
        return chiffres
    }
}
