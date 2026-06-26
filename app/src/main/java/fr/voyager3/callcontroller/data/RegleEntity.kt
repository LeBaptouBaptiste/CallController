package fr.voyager3.callcontroller.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.Regle
import fr.voyager3.callcontroller.matching.TypeRegle

/** Représentation persistée d'une [Regle] (table Room). */
@Entity(tableName = "regles")
data class RegleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TypeRegle,
    val valeur: String,
    val action: ActionRegle = ActionRegle.BLOQUER,
    val presetId: String? = null,
    val actif: Boolean = true,
) {
    fun versDomaine(): Regle = Regle(id, type, valeur, action, presetId, actif)

    companion object {
        fun depuisDomaine(regle: Regle): RegleEntity =
            RegleEntity(regle.id, regle.type, regle.valeur, regle.action, regle.presetId, regle.actif)
    }
}
