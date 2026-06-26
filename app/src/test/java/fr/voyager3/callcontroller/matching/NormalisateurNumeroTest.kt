package fr.voyager3.callcontroller.matching

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalisateurNumeroTest {

    @Test
    fun `format national inchange`() {
        assertEquals("0162123456", NormalisateurNumero.normaliser("0162123456"))
    }

    @Test
    fun `format international plus 33 converti en national`() {
        assertEquals("0162123456", NormalisateurNumero.normaliser("+33 1 62 12 34 56"))
    }

    @Test
    fun `format 0033 converti en national`() {
        assertEquals("0162123456", NormalisateurNumero.normaliser("0033162123456"))
    }

    @Test
    fun `retire espaces points et tirets`() {
        assertEquals("0162123456", NormalisateurNumero.normaliser("01.62-12 34.56"))
    }

    @Test
    fun `chaine sans chiffre donne vide`() {
        assertEquals("", NormalisateurNumero.normaliser("inconnu"))
    }
}
