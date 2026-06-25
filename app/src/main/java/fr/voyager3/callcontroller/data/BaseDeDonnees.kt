package fr.voyager3.callcontroller.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RegleEntity::class, AppelBloqueEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Convertisseurs::class)
abstract class BaseDeDonnees : RoomDatabase() {
    abstract fun regleDao(): RegleDao
    abstract fun appelBloqueDao(): AppelBloqueDao
}
