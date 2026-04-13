package com.example.emptyactivity.domain.model

data class GithubRepo(
    val id: Int,
    val name: String,
    val owner: String,
    val language: String,
    val stars: Int,
    val forks: Int,
    val openIssues: Int,
    val url: String,
    val description: String,
    val defaultBranch: String
)