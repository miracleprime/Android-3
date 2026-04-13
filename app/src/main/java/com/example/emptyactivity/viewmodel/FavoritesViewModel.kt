package com.example.emptyactivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val favorites = favoritesRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeIsFavorite(repoId: Int): Flow<Boolean> {
        return favoritesRepository.observeIsFavorite(repoId)
    }

    fun addToFavorites(repo: GithubRepo) {
        viewModelScope.launch {
            favoritesRepository.addToFavorites(repo)
        }
    }

    fun removeFromFavorites(repoId: Int) {
        viewModelScope.launch {
            favoritesRepository.removeFromFavorites(repoId)
        }
    }

    companion object {
        fun factory(
            favoritesRepository: FavoritesRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FavoritesViewModel(
                        favoritesRepository = favoritesRepository
                    ) as T
                }
            }
        }
    }
}
