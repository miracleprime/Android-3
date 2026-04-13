package com.example.emptyactivity.domain.repository

import com.example.emptyactivity.domain.model.GithubRepo

interface GithubRepository {
    suspend fun getUserRepos(username: String): Result<List<GithubRepo>>
}
