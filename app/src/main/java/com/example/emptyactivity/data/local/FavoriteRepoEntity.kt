package com.example.emptyactivity.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.emptyactivity.domain.model.GithubRepo

@Entity(tableName = "favorite_repos")
data class FavoriteRepoEntity(
    @PrimaryKey val id: Int,
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

fun FavoriteRepoEntity.toDomain(): GithubRepo {
    return GithubRepo(
        id = id,
        name = name,
        owner = owner,
        language = language,
        stars = stars,
        forks = forks,
        openIssues = openIssues,
        url = url,
        description = description,
        defaultBranch = defaultBranch
    )
}

fun GithubRepo.toEntity(): FavoriteRepoEntity {
    return FavoriteRepoEntity(
        id = id,
        name = name,
        owner = owner,
        language = language,
        stars = stars,
        forks = forks,
        openIssues = openIssues,
        url = url,
        description = description,
        defaultBranch = defaultBranch
    )
}