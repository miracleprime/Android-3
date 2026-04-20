package com.example.emptyactivity.data.repository

import com.example.emptyactivity.data.local.FavoriteRepoDao
import com.example.emptyactivity.data.local.toDomain
import com.example.emptyactivity.data.local.toEntity
import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteRepoDao
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<GithubRepo>> {
        return dao.observeFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeIsFavorite(repoId: Int): Flow<Boolean> {
        return dao.observeIsFavorite(repoId)
    }

    override suspend fun addToFavorites(repo: GithubRepo) {
        dao.insert(repo.toEntity())
    }

    override suspend fun removeFromFavorites(repoId: Int) {
        dao.deleteById(repoId)
    }
}
