package com.example.emptyactivity.data.remote

import com.example.emptyactivity.data.remote.dto.GithubRepoDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {


    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 50
    ): List<GithubRepoDto>
}