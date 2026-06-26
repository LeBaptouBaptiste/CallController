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
}
