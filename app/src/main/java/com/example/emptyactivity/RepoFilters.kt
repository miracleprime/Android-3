package com.example.emptyactivity.domain.model

data class RepoFilters(
    val username: String = DEFAULT_USERNAME,
    val repoNameQuery: String = "",
    val minStars: Int = 0
) {
    fun isDefault(): Boolean {
        return username == DEFAULT_USERNAME && repoNameQuery.isBlank() && minStars == 0
    }

    companion object {
        const val DEFAULT_USERNAME = "dekabrsky"
    }
}