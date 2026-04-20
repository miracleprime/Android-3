package com.example.emptyactivity.viewmodel

import com.example.emptyactivity.domain.model.GithubRepo

sealed interface RepoUiState {
    data object Idle : RepoUiState
    data object Loading : RepoUiState
    data class Success(val repos: List<GithubRepo>) : RepoUiState
    data class Error(val message: String) : RepoUiState
}