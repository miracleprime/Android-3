package com.example.emptyactivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emptyactivity.data.remote.ApiFactory
import com.example.emptyactivity.data.repository.GithubRepositoryImpl
import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.domain.usecase.GetUserReposUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReposViewModel : ViewModel() {

    private val useCase = GetUserReposUseCase(
        GithubRepositoryImpl(ApiFactory.api)
    )

    private val _username = MutableStateFlow("dekabrsky")
    val username = _username.asStateFlow()

    private val _uiState = MutableStateFlow<RepoUiState>(RepoUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun loadRepos() {
        val currentName = _username.value.trim()

        if (currentName.isBlank()) {
            _uiState.value = RepoUiState.Error("Введите username, например google")
            return
        }

        viewModelScope.launch {
            _uiState.value = RepoUiState.Loading

            useCase(currentName)
                .onSuccess { repos ->
                    _uiState.value = RepoUiState.Success(repos)
                }
                .onFailure { error ->
                    _uiState.value = RepoUiState.Error(
                        error.message ?: "Не удалось загрузить данные"
                    )
                }
        }
    }

    fun getRepoById(id: Int): GithubRepo? {
        val successState = uiState.value as? RepoUiState.Success
        return successState?.repos?.find { it.id == id }
    }
}