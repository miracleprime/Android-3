package com.example.emptyactivity.domain.usecase

import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.domain.repository.GithubRepository

class GetUserReposUseCase(
    private val repository: GithubRepository
) {
    suspend operator fun invoke(username: String): Result<List<GithubRepo>> {
        return repository.getUserRepos(username)
    }
}