package com.example.emptyactivity.di

import android.content.Context
import androidx.room.Room
import com.example.emptyactivity.data.local.AppDatabase
import com.example.emptyactivity.data.preferences.RepoFiltersStorage
import com.example.emptyactivity.data.remote.ApiFactory
import com.example.emptyactivity.data.repository.FavoritesRepositoryImpl
import com.example.emptyactivity.data.repository.GithubRepositoryImpl
import com.example.emptyactivity.domain.repository.FavoritesRepository
import com.example.emptyactivity.domain.repository.GithubRepository
import com.example.emptyactivity.domain.usecase.GetUserReposUseCase

// Очень простой ручной DI.
// Не Hilt, но это тоже dependency injection:
// зависимости создаются в одном месте и потом передаются в нужные классы.
class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    val filtersBadgeCache = FiltersBadgeCache()
    val repoFiltersStorage = RepoFiltersStorage(applicationContext)

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "repos_database"
        ).build()
    }

    val githubRepository: GithubRepository by lazy {
        GithubRepositoryImpl(ApiFactory.api)
    }

    val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepositoryImpl(database.favoriteRepoDao())
    }

    val getUserReposUseCase: GetUserReposUseCase by lazy {
        GetUserReposUseCase(githubRepository)
    }
}