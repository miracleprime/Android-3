package com.example.emptyactivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emptyactivity.data.preferences.RepoFiltersStorage
import com.example.emptyactivity.di.FiltersBadgeCache
import com.example.emptyactivity.domain.model.RepoFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repoFiltersStorage: RepoFiltersStorage,
    private val filtersBadgeCache: FiltersBadgeCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val shouldShowBadge = filtersBadgeCache.shouldShowBadge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            repoFiltersStorage.filtersFlow.collectLatest { filters ->
                _uiState.value = SettingsUiState(
                    username = filters.username,
                    repoNameQuery = filters.repoNameQuery,
                    minStarsText = filters.minStars.toString()
                )
                filtersBadgeCache.setShouldShowBadge(filters.isDefault().not())
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onRepoNameQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(repoNameQuery = value)
    }

    fun onMinStarsChange(value: String) {
        if (value.all { it.isDigit() } || value.isEmpty()) {
            _uiState.value = _uiState.value.copy(minStarsText = value)
        }
    }

    fun saveFilters() {
        viewModelScope.launch {
            val preparedFilters = RepoFilters(
                username = _uiState.value.username.trim().ifBlank { RepoFilters.DEFAULT_USERNAME },
                repoNameQuery = _uiState.value.repoNameQuery.trim(),
                minStars = _uiState.value.minStarsText.toIntOrNull() ?: 0
            )
            repoFiltersStorage.saveFilters(preparedFilters)
            filtersBadgeCache.setShouldShowBadge(preparedFilters.isDefault().not())
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            val defaultFilters = RepoFilters()
            repoFiltersStorage.saveFilters(defaultFilters)
            filtersBadgeCache.setShouldShowBadge(false)
        }
    }

    companion object {
        fun factory(
            repoFiltersStorage: RepoFiltersStorage,
            filtersBadgeCache: FiltersBadgeCache
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        repoFiltersStorage = repoFiltersStorage,
                        filtersBadgeCache = filtersBadgeCache
                    ) as T
                }
            }
        }
    }
}