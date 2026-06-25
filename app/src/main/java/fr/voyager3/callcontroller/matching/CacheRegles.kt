package fr.voyager3.callcontroller.matching

import java.util.concurrent.atomic.AtomicReference

/**
 * Instantané en mémoire du moteur de filtrage, lu par le service de screening
 * dans son hot path (donc sans I/O ni accès base). Mis à jour par le dépôt
 * quand les règles changent. Thread-safe via [AtomicReference].
 */
object CacheRegles {

    private val reference = AtomicReference(MoteurDeFiltrage(emptyList()))

    val moteur: MoteurDeFiltrage get() = reference.get()

    fun remplacer(regles: List<Regle>) {
        reference.set(MoteurDeFiltrage(regles))
    }
}
