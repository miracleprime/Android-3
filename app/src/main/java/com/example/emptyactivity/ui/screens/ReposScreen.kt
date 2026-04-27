package com.example.emptyactivity.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emptyactivity.R
import com.example.emptyactivity.domain.model.RepoFilters
import com.example.emptyactivity.ui.components.RepoListItem
import com.example.emptyactivity.viewmodel.RepoUiState
import com.example.emptyactivity.viewmodel.ReposViewModel

private val AppBarBlue = Color(0xFF3F51B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposScreen(
    viewModel: ReposViewModel,
    onRepoClick: (Int) -> Unit,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val shouldShowBadge by viewModel.shouldShowBadge.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.repos_title)) },
                actions = {
                    BadgedBox(
                        badge = {
                            if (shouldShowBadge) Badge()
                        }
                    ) {
                        TextButton(onClick = onOpenSettings) {
                            Text(stringResource(R.string.filters_button), color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBarBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            FiltersSummaryCard(filters = filters)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.refresh() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.refresh_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                RepoUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.repos_not_loaded))
                    }
                }

                RepoUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is RepoUiState.Error -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text(stringResource(R.string.retry_button))
                        }
                    }
                }

                is RepoUiState.Success -> {
                    if (state.repos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.repos_not_found_by_filters))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.repos) { repo ->
                                RepoListItem(
                                    repo = repo,
                                    onClick = { onRepoClick(repo.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FiltersSummaryCard(filters: RepoFilters) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.filters_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.filters_username, filters.username))
            Text(
                stringResource(
                    R.string.filters_name_contains,
                    if (filters.repoNameQuery.isBlank()) {
                        stringResource(R.string.filters_any)
                    } else {
                        filters.repoNameQuery
                    }
                )
            )
            Text(stringResource(R.string.filters_min_stars, filters.minStars))
        }
    }
}