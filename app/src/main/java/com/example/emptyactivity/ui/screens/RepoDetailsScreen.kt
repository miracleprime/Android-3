package com.example.emptyactivity.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emptyactivity.R
import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.ui.components.InfoRow
import com.example.emptyactivity.viewmodel.FavoritesViewModel

private val AppBarBlue = Color(0xFF3F51B5)
private val LinkBlue = Color(0xFF1E88E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailsScreen(
    repo: GithubRepo,
    favoritesViewModel: FavoritesViewModel,
    onBackClick: () -> Unit
) {
    val isFavoriteFlow = remember(repo.id) { favoritesViewModel.observeIsFavorite(repo.id) }
    val isFavorite by isFavoriteFlow.collectAsStateWithLifecycle(initialValue = false)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                title = { Text(stringResource(R.string.details_title)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBarBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Button(
                    onClick = {
                        if (isFavorite) {
                            favoritesViewModel.removeFromFavorites(repo.id)
                        } else {
                            favoritesViewModel.addToFavorites(repo)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isFavorite) {
                            stringResource(R.string.details_remove_from_favorites)
                        } else {
                            stringResource(R.string.details_add_to_favorites)
                        }
                    )
                }
            }

            repo.owner?.takeIf { it.isNotBlank() }?.let { owner ->
                item { InfoRow(label = stringResource(R.string.details_owner), value = owner) }
            }

            repo.language?.takeIf { it.isNotBlank() }?.let { language ->
                item { InfoRow(label = stringResource(R.string.details_language), value = language) }
            }

            item { InfoRow(label = stringResource(R.string.details_stars), value = repo.stars.toString()) }
            item { InfoRow(label = stringResource(R.string.details_forks), value = repo.forks.toString()) }
            item { InfoRow(label = stringResource(R.string.details_open_issues), value = repo.openIssues.toString()) }
            item { InfoRow(label = stringResource(R.string.details_default_branch), value = repo.defaultBranch) }

            item {
                Text(
                    text = stringResource(R.string.details_description),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = repo.description)
            }

            item {
                Text(
                    text = stringResource(R.string.details_link),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = repo.url, color = LinkBlue)
            }
        }
    }
}