package com.example.emptyactivity.data.remote.dto

import com.example.emptyactivity.domain.model.GithubRepo
import com.google.gson.annotations.SerializedName

data class GithubRepoDto(
    val id: Int,
    val name: String,
    val owner: OwnerDto?,
    val language: String?,
    @SerializedName("stargazers_count")
    val stargazersCount: Int?,
    @SerializedName("forks_count")
    val forksCount: Int?,
    @SerializedName("open_issues_count")
    val openIssuesCount: Int?,
    @SerializedName("html_url")
    val htmlUrl: String?,
    val description: String?,
    @SerializedName("default_branch")
    val defaultBranch: String?
)

data class OwnerDto(
    val login: String?
)

fun GithubRepoDto.toDomain(): GithubRepo {
    return GithubRepo(
        id = id,
        name = name,
        owner = owner?.login ?: "unknown",
        language = language ?: "Unknown",
        stars = stargazersCount ?: 0,
        forks = forksCount ?: 0,
        openIssues = openIssuesCount ?: 0,
        url = htmlUrl ?: "",
        description = description ?: "Описание отсутствует",
        defaultBranch = defaultBranch ?: "main"
    )
}