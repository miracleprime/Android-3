package com.example.emptyactivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emptyactivity.data.preferences.RepoFiltersStorage
import com.example.emptyactivity.di.FiltersBadgeCache
import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.domain.model.RepoFilters
import com.example.emptyactivity.domain.usecase.GetUserReposUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReposViewModel(
    private val getUserReposUseCase: GetUserReposUseCase,
    private val repoFiltersStorage: RepoFiltersStorage,
    private val filtersBadgeCache: FiltersBadgeCache
) : ViewModel() {

    private val _filters = MutableStateFlow(RepoFilters())
    val filters: StateFlow<RepoFilters> = _filters.asStateFlow()

    private val _uiState = MutableStateFlow<RepoUiState>(RepoUiState.Idle)
    val uiState: StateFlow<RepoUiState> = _uiState.asStateFlow()

    // Экран списка и экран настроек читают одно и то же значение бейджа.
    val shouldShowBadge = filtersBadgeCache.shouldShowBadge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            repoFiltersStorage.filtersFlow.collectLatest { savedFilters ->
                _filters.value = savedFilters
                filtersBadgeCache.setShouldShowBadge(savedFilters.isDefault().not())
                loadReposInternal(savedFilters)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadReposInternal(_filters.value)
        }
    }

    private suspend fun loadReposInternal(filters: RepoFilters) {
        val username = filters.username.trim()

        if (username.isBlank()) {
            _uiState.value = RepoUiState.Error("Введите GitHub username")
            return
        }

        _uiState.value = RepoUiState.Loading

        getUserReposUseCase(username)
            .onSuccess { repos ->
                val filteredRepos = applyLocalFilters(repos, filters)
                _uiState.value = RepoUiState.Success(filteredRepos)
            }
            .onFailure { throwable ->
                _uiState.value = RepoUiState.Error(
                    throwable.message ?: "Не удалось загрузить данные"
                )
            }
    }

    private fun applyLocalFilters(repos: List<GithubRepo>, filters: RepoFilters): List<GithubRepo> {
        return repos.filter { repo ->
            val matchesName = filters.repoNameQuery.isBlank() ||
                    repo.name.contains(filters.repoNameQuery, ignoreCase = true)

            val matchesStars = repo.stars >= filters.minStars

            matchesName && matchesStars
        }
    }

    fun getRepoById(id: Int): GithubRepo? {
        val successState = uiState.value as? RepoUiState.Success
        return successState?.repos?.find { it.id == id }
    }

    companion object {
        fun factory(
            getUserReposUseCase: GetUserReposUseCase,
            repoFiltersStorage: RepoFiltersStorage,
            filtersBadgeCache: FiltersBadgeCache
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReposViewModel(
                        getUserReposUseCase = getUserReposUseCase,
                        repoFiltersStorage = repoFiltersStorage,
                        filtersBadgeCache = filtersBadgeCache
                    ) as T
                }
            }
        }
    }
}