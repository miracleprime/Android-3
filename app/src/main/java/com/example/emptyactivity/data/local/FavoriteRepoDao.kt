package com.example.emptyactivity.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRepoDao {

    @Query("SELECT * FROM favorite_repos ORDER BY name ASC")
    fun observeFavorites(): Flow<List<FavoriteRepoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repos WHERE id = :repoId)")
    fun observeIsFavorite(repoId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repo: FavoriteRepoEntity)

    @Query("DELETE FROM favorite_repos WHERE id = :repoId")
    suspend fun deleteById(repoId: Int)
}