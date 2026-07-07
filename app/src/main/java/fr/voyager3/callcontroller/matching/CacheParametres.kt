package fr.voyager3.callcontroller.matching

/**
 * Instantané en mémoire des paramètres lus par le service de screening dans son
 * hot path (sans I/O). Mis à jour par l'application quand un paramètre change.
 */
object CacheParametres {
    @Volatile
    var bloquerMasques: Boolean = false
}
