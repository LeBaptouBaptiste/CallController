package fr.voyager3.callcontroller.data

/** Entrée du journal : un appel rejeté par le filtrage. */
data class AppelBloque(
    val id: Long = 0,
    val numero: String,
    val horodatage: Long,
    val motif: String? = null,
)
