package fr.voyager3.callcontroller.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppelBloqueDao {

    @Query("SELECT * FROM appels_bloques ORDER BY horodatage DESC LIMIT 500")
    fun observerRecents(): Flow<List<AppelBloqueEntity>>

    @Insert
    suspend fun inserer(appel: AppelBloqueEntity)

    @Query("DELETE FROM appels_bloques")
    suspend fun vider()
}
