package com.example.emptyactivity.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.emptyactivity.domain.model.RepoFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Важный момент из документации DataStore:
// такой delegate нужно объявлять один раз на верхнем уровне файла.
private val Context.dataStore by preferencesDataStore(name = "repo_filters")

class RepoFiltersStorage(
    private val context: Context
) {

    private object Keys {
        val USERNAME = stringPreferencesKey("username")
        val REPO_NAME_QUERY = stringPreferencesKey("repo_name_query")
        val MIN_STARS = intPreferencesKey("min_stars")
    }

    val filtersFlow: Flow<RepoFilters> = context.dataStore.data.map { preferences: Preferences ->
        RepoFilters(
            username = preferences[Keys.USERNAME] ?: RepoFilters.DEFAULT_USERNAME,
            repoNameQuery = preferences[Keys.REPO_NAME_QUERY] ?: "",
            minStars = preferences[Keys.MIN_STARS] ?: 0
        )
    }

    suspend fun saveFilters(filters: RepoFilters) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USERNAME] = filters.username
            preferences[Keys.REPO_NAME_QUERY] = filters.repoNameQuery
            preferences[Keys.MIN_STARS] = filters.minStars
        }
    }
}