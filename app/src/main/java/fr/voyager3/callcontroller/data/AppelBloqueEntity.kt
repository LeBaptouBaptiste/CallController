package fr.voyager3.callcontroller.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Journal persisté localement (donnée de l'utilisateur, jamais exfiltrée). Le
 * numéro complet est conservé ici car c'est l'objet même de la fonctionnalité.
 */
@Entity(tableName = "appels_bloques")
data class AppelBloqueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numero: String,
    val horodatage: Long,
    val motif: String? = null,
) {
    fun versDomaine(): AppelBloque = AppelBloque(id, numero, horodatage, motif)
}
