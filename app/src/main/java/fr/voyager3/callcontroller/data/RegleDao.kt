package fr.voyager3.callcontroller.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RegleDao {

    @Query("SELECT * FROM regles ORDER BY action, id")
    fun observerToutes(): Flow<List<RegleEntity>>

    @Query("SELECT * FROM regles WHERE actif = 1")
    fun observerActives(): Flow<List<RegleEntity>>

    @Query("SELECT COUNT(*) FROM regles")
    suspend fun compter(): Int

    @Upsert
    suspend fun upsert(regles: List<RegleEntity>)

    @Insert
    suspend fun inserer(regle: RegleEntity): Long

    @Query("UPDATE regles SET actif = :actif WHERE id = :id")
    suspend fun definirActif(id: Long, actif: Boolean)

    @Query("DELETE FROM regles WHERE id = :id")
    suspend fun supprimer(id: Long)
}
