package fr.voyager3.callcontroller.di

import android.content.Context
import androidx.room.Room
import fr.voyager3.callcontroller.data.BaseDeDonnees
import fr.voyager3.callcontroller.data.ChargeurPreset
import fr.voyager3.callcontroller.data.DepotJournal
import fr.voyager3.callcontroller.data.DepotRegles

/**
 * Conteneur d'injection de dépendances manuel (YAGNI : pas de framework DI tant
 * que le câblage reste simple). Instancié une fois par l'[android.app.Application].
 */
class AppContainer(context: Context) {

    private val baseDeDonnees: BaseDeDonnees =
        Room.databaseBuilder(context, BaseDeDonnees::class.java, "callcontroller.db")
            // Pré-release : on accepte de repartir de zéro à chaque changement de schéma.
            // À remplacer par de vraies migrations avant la première publication.
            .fallbackToDestructiveMigration()
            .build()

    private val chargeurPreset = ChargeurPreset(context)

    val depotRegles: DepotRegles = DepotRegles(baseDeDonnees.regleDao(), chargeurPreset)

    val depotJournal: DepotJournal = DepotJournal(baseDeDonnees.appelBloqueDao())
}
