package fr.voyager3.callcontroller.matching

/**
 * Instantané en mémoire des réglages lus par le service de screening dans son
 * hot path (sans I/O). Mis à jour par l'application quand un réglage change.
 */
object CacheReglages {
    @Volatile
    var bloquerMasques: Boolean = false
}
