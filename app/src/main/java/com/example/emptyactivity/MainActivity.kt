package com.example.emptyactivity

import com.example.emptyactivity.ui.components.InfoRow
import com.example.emptyactivity.ui.components.RepoListItem
import com.example.emptyactivity.ui.screens.ReposScreen
import com.example.emptyactivity.ui.screens.SettingsScreen
import android.Manifest
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.emptyactivity.domain.model.DownloadedFile
import com.example.emptyactivity.domain.model.GithubRepo
import com.example.emptyactivity.domain.model.RepoFilters
import com.example.emptyactivity.ui.theme.EmptyActivityTheme
import com.example.emptyactivity.viewmodel.FavoritesViewModel
import com.example.emptyactivity.viewmodel.ProfileEvent
import com.example.emptyactivity.viewmodel.ProfileViewModel
import com.example.emptyactivity.viewmodel.RepoUiState
import com.example.emptyactivity.viewmodel.ReposViewModel
import com.example.emptyactivity.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar
import android.app.AlarmManager
import androidx.compose.ui.res.stringResource
import com.example.emptyactivity.ui.screens.FavoritesScreen
import com.example.emptyactivity.ui.screens.RepoDetailsScreen
import com.example.emptyactivity.ui.screens.ProfileRoute
import com.example.emptyactivity.ui.screens.EditProfileRoute


private val AppBarBlue = Color(0xFF3F51B5)
private val AvatarBlue = Color(0xFF9FA8DA)
private val LinkBlue = Color(0xFF1E88E5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmptyActivityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val titleRes: Int) {
    data object Repos : Screen("repos", R.string.nav_repos)
    data object Favorites : Screen("favorites", R.string.nav_favorites)
    data object Profile : Screen("profile", R.string.nav_profile)
    data object Settings : Screen("settings", R.string.settings_title)
    data object EditProfile : Screen("edit_profile", R.string.edit_profile_title)
    data object RepoDetails : Screen("repo_details/{repoId}", R.string.details_title) {
        fun createRoute(repoId: Int): String = "repo_details/$repoId"
    }
}

@Composable
fun App() {
    val application = applicationContext() as RepoApplication
    val container = application.appContainer

    val reposViewModel: ReposViewModel = viewModel(
        factory = ReposViewModel.factory(
            getUserReposUseCase = container.getUserReposUseCase,
            repoFiltersStorage = container.repoFiltersStorage,
            filtersBadgeCache = container.filtersBadgeCache
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(
            repoFiltersStorage = container.repoFiltersStorage,
            filtersBadgeCache = container.filtersBadgeCache
        )
    )

    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.factory(
            favoritesRepository = container.favoritesRepository
        )
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            observeUserProfileUseCase = container.observeUserProfileUseCase,
            saveUserProfileUseCase = container.saveUserProfileUseCase,
            downloadResumeFileUseCase = container.downloadResumeFileUseCase,
            userProfileRepository = container.userProfileRepository,
            favoritePairAlarmScheduler = container.favoritePairAlarmScheduler
        )
    )

    val navController = rememberNavController()
    val bottomScreens = listOf(Screen.Repos, Screen.Favorites, Screen.Profile)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                when (screen) {
                                    Screen.Repos -> Icon(Icons.Default.List, contentDescription = stringResource(R.string.nav_repos))
                                    Screen.Favorites -> Icon(Icons.Default.Star, contentDescription = stringResource(R.string.nav_favorites))
                                    Screen.Profile -> Text("👤")
                                    else -> Unit
                                }
                            },
                            label = { Text(stringResource(screen.titleRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Repos.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Repos.route) {
                ReposScreen(
                    viewModel = reposViewModel,
                    onRepoClick = { repoId -> navController.navigate(Screen.RepoDetails.createRoute(repoId)) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    favoritesViewModel = favoritesViewModel,
                    onRepoClick = { repoId -> navController.navigate(Screen.RepoDetails.createRoute(repoId)) }
                )
            }

            composable(Screen.Profile.route) {
                ProfileRoute(
                    viewModel = profileViewModel,
                    onEditClick = {
                        profileViewModel.startEditing()
                        navController.navigate(Screen.EditProfile.route)
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileRoute(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onDoneClick = {
                        profileViewModel.saveProfile()
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onApplyClick = {
                        settingsViewModel.saveFilters()
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.RepoDetails.route) { backStackEntry ->
                val repoId = backStackEntry.arguments?.getString("repoId")?.toIntOrNull()
                val repo = repoId?.let { reposViewModel.getRepoById(it) }
                    ?: favoritesViewModel.favorites.value.find { it.id == repoId }

                if (repo != null) {
                    RepoDetailsScreen(
                        repo = repo,
                        favoritesViewModel = favoritesViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.repo_not_found))
                    }
                }
            }
        }
    }
}

@Composable
private fun applicationContext(): android.app.Application {
    return LocalContext.current.applicationContext as android.app.Application
}

