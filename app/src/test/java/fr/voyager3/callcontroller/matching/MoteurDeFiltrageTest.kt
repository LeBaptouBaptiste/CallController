package fr.voyager3.callcontroller.matching

import org.junit.Assert.assertEquals
import org.junit.Test

class MoteurDeFiltrageTest {

    private fun moteur(vararg regles: Regle) = MoteurDeFiltrage(regles.toList())

    @Test
    fun `prefixe arcep est rejete`() {
        val m = moteur(Regle(type = TypeRegle.PREFIXE, valeur = "0162"))
        assertEquals(Decision.REJETER, m.evaluer("0162123456"))
    }

    @Test
    fun `prefixe arcep rejete aussi au format international`() {
        val m = moteur(Regle(type = TypeRegle.PREFIXE, valeur = "0162"))
        assertEquals(Decision.REJETER, m.evaluer("+33162123456"))
    }

    @Test
    fun `numero hors prefixe est autorise`() {
        val m = moteur(Regle(type = TypeRegle.PREFIXE, valeur = "0162"))
        assertEquals(Decision.AUTORISER, m.evaluer("0612345678"))
    }

    @Test
    fun `regle inactive est ignoree`() {
        val m = moteur(Regle(type = TypeRegle.PREFIXE, valeur = "0162", actif = false))
        assertEquals(Decision.AUTORISER, m.evaluer("0162123456"))
    }

    @Test
    fun `regex valide rejette`() {
        val m = moteur(Regle(type = TypeRegle.REGEX, valeur = "^0(948|949)\\d{6}$"))
        assertEquals(Decision.REJETER, m.evaluer("0948123456"))
    }

    @Test
    fun `regex invalide est ignoree sans planter`() {
        val m = moteur(Regle(type = TypeRegle.REGEX, valeur = "([a-z"))
        assertEquals(Decision.AUTORISER, m.evaluer("0148123456"))
    }

    @Test
    fun `liste blanche est prioritaire sur le blocage`() {
        val m = moteur(
            Regle(type = TypeRegle.PREFIXE, valeur = "0162", action = ActionRegle.BLOQUER),
            Regle(type = TypeRegle.PREFIXE, valeur = "0162123", action = ActionRegle.AUTORISER),
        )
        assertEquals(Decision.AUTORISER, m.evaluer("0162123456"))
    }

    @Test
    fun `le motif renseigne le prefixe bloquant`() {
        val m = moteur(Regle(type = TypeRegle.PREFIXE, valeur = "0162"))
        val resultat = m.evaluerDetail("0162123456")
        assertEquals(Decision.REJETER, resultat.decision)
        assertEquals("0162", resultat.motif)
    }

    // Un motif à backtracking catastrophique confronté à une entrée hostile très
    // longue : sur un moteur classique cela gèle le hot path (ReDoS). RE2 (temps
    // linéaire) et la borne de longueur garantissent une évaluation immédiate ;
    // le timeout échouerait en cas de régression.
    @Test(timeout = 2000)
    fun `une regex catastrophique ne provoque pas de ReDoS`() {
        val m = moteur(Regle(type = TypeRegle.REGEX, valeur = "(.*[0-9]){15}z"))
        assertEquals(Decision.AUTORISER, m.evaluer("9".repeat(100_000)))
    }

    @Test
    fun `un numero anormalement long est borne avant matching`() {
        val m = moteur(Regle(type = TypeRegle.PREFIXE, valeur = "0162"))
        // La troncature préserve la détection du préfixe et borne le coût.
        assertEquals(Decision.REJETER, m.evaluer("0162" + "9".repeat(1_000)))
    }
}
