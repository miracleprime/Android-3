package com.example.emptyactivity.data.repository

import com.example.emptyactivity.data.remote.GitHubApi
import com.example.emptyactivity.data.remote.dto.toDomain
import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.domain.repository.GithubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GithubRepositoryImpl(
    private val api: GitHubApi
) : GithubRepository {

    override suspend fun getUserRepos(username: String): Result<List<GithubRepo>> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Введите username GitHub"))
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                api.getUserRepos(username = username.trim())
                    .map { dto -> dto.toDomain() }
            }
        }
    }
}