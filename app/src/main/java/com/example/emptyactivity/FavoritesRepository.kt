package com.example.emptyactivity.domain.repository

import com.example.emptyactivity.domain.model.GithubRepo
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<GithubRepo>>
    fun observeIsFavorite(repoId: Int): Flow<Boolean>
    suspend fun addToFavorites(repo: GithubRepo)
    suspend fun removeFromFavorites(repoId: Int)
}