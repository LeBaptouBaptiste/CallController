package fr.voyager3.callcontroller.data

import androidx.room.TypeConverter
import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.TypeRegle

/** Conversions d'enums pour Room (stockés en texte). */
class Convertisseurs {
    @TypeConverter
    fun versType(valeur: String): TypeRegle = TypeRegle.valueOf(valeur)

    @TypeConverter
    fun depuisType(type: TypeRegle): String = type.name

    @TypeConverter
    fun versAction(valeur: String): ActionRegle = ActionRegle.valueOf(valeur)

    @TypeConverter
    fun depuisAction(action: ActionRegle): String = action.name
}
