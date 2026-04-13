package com.example.emptyactivity

import android.Manifest
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
import androidx.compose.runtime.saveable.rememberSaveable
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

sealed class Screen(val route: String, val title: String) {
    data object Repos : Screen("repos", "Список")
    data object Favorites : Screen("favorites", "Избранное")
    data object Profile : Screen("profile", "Профиль")
    data object Settings : Screen("settings", "Фильтры")
    data object EditProfile : Screen("edit_profile", "Редактирование")
    data object RepoDetails : Screen("repo_details/{repoId}", "Карточка") {
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
            userProfileRepository = container.userProfileRepository
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
                                    Screen.Repos -> Icon(Icons.Default.List, contentDescription = "Список")
                                    Screen.Favorites -> Icon(Icons.Default.Star, contentDescription = "Избранное")
                                    Screen.Profile -> Text("👤")
                                    else -> Unit
                                }
                            },
                            label = { Text(screen.title) }
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
                    onRepoClick = { repoId ->
                        navController.navigate(Screen.RepoDetails.createRoute(repoId))
                    },
                    onOpenSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    favoritesViewModel = favoritesViewModel,
                    onRepoClick = { repoId ->
                        navController.navigate(Screen.RepoDetails.createRoute(repoId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onEditClick = {
                        profileViewModel.startEditing()
                        navController.navigate(Screen.EditProfile.route)
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
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
                val repoId = backStackEntry.arguments
                    ?.getString("repoId")
                    ?.toIntOrNull()

                val repo = repoId?.let { reposViewModel.getRepoById(it) }
                    ?: favoritesViewModel.favorites.value.find { it.id == repoId }

                if (repo != null) {
                    RepoDetailsScreen(
                        repo = repo,
                        favoritesViewModel = favoritesViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Репозиторий не найден")
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
                title = { Text("Репозитории GitHub") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (shouldShowBadge) Badge()
                        }
                    ) {
                        TextButton(onClick = onOpenSettings) {
                            Text("Фильтры", color = Color.White)
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
                Text("Обновить список")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                RepoUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Данные пока не загружены")
                    }
                }
                RepoUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RepoUiState.Error -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.refresh() }) { Text("Повторить") }
                    }
                }
                is RepoUiState.Success -> {
                    if (state.repos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("По этим фильтрам ничего не найдено")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.repos) { repo ->
                                RepoListItem(repo = repo, onClick = { onRepoClick(repo.id) })
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
                text = "Текущие настройки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Username: ${filters.username}")
            Text("Название содержит: ${if (filters.repoNameQuery.isBlank()) "любое" else filters.repoNameQuery}")
            Text("Минимум звёзд: ${filters.minStars}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shouldShowBadge by viewModel.shouldShowBadge.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Фильтры") },
                actions = { if (shouldShowBadge) Badge() },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBarBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Здесь мы меняем настройки списка. После нажатия «Готово» список перезагрузится.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("GitHub username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.repoNameQuery,
                onValueChange = viewModel::onRepoNameQueryChange,
                label = { Text("Название содержит") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.minStarsText,
                onValueChange = viewModel::onMinStarsChange,
                label = { Text("Минимум звёзд") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onApplyClick, modifier = Modifier.fillMaxWidth()) { Text("Готово") }
            TextButton(onClick = { viewModel.resetToDefault() }, modifier = Modifier.fillMaxWidth()) {
                Text("Сбросить к значениям по умолчанию")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    onRepoClick: (Int) -> Unit
) {
    val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Избранное") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBarBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Пока нет избранных репозиториев")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favorites) { repo ->
                    RepoListItem(repo = repo, onClick = { onRepoClick(repo.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ProfileEvent.OpenDownloadedFile -> {
                    openDownloadedFile(context, event.file)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Профиль") },
                actions = {
                    TextButton(onClick = onEditClick) {
                        Text("Редактировать", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBarBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarView(avatarUri = uiState.profile.avatarUri, size = 120)

            if (uiState.profile.isEmpty()) {
                Text("Профиль пока пустой")
            } else {
                Text(
                    text = if (uiState.profile.fullName.isBlank()) "ФИО не заполнено" else uiState.profile.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.profile.position.isNotBlank()) {
                    Text(uiState.profile.position, color = Color.Gray)
                }

                ProfileInfoCard(
                    title = "Ссылка на резюме",
                    value = if (uiState.profile.resumeUrl.isBlank()) "Не указана" else uiState.profile.resumeUrl
                )

                Button(
                    onClick = { viewModel.openResume() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isDownloadingResume) "Скачивание..." else "Резюме")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAvatarSourceDialog by remember { mutableStateOf(false) }
    var storagePermissionAsked by rememberSaveable { mutableStateOf(false) }

    val galleryPermission = galleryPermissionForCurrentAndroid()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onGalleryImageChosen(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onCameraCaptureResult(success)
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Без доступа к хранилищу редактирование недоступно", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uriString = viewModel.createCameraOutputUri()
            uriString?.let { cameraLauncher.launch(Uri.parse(it)) }
        } else {
            Toast.makeText(context, "Без доступа к камере нельзя сделать фото", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startEditing()
        if (!storagePermissionAsked) {
            storagePermissionAsked = true
            if (ContextCompat.checkSelfPermission(context, galleryPermission) != PackageManager.PERMISSION_GRANTED) {
                storagePermissionLauncher.launch(galleryPermission)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is ProfileEvent.ShowMessage) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Редактирование профиля") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBarBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AvatarView(
                avatarUri = uiState.editAvatarUri,
                size = 120,
                onClick = { showAvatarSourceDialog = true }
            )
            Text("Нажми на фото, чтобы выбрать из галереи или камеры")

            OutlinedTextField(
                value = uiState.editFullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("ФИО") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.editPosition,
                onValueChange = viewModel::onPositionChange,
                label = { Text("Должность / роль") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.editResumeUrl,
                onValueChange = viewModel::onResumeUrlChange,
                label = { Text("Ссылка на резюме / портфолио") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Button(
                onClick = onDoneClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Готово")
            }
        }
    }

    if (showAvatarSourceDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarSourceDialog = false },
            title = { Text("Откуда взять аватарку?") },
            text = { Text("Можно выбрать фото из галереи или сделать новый снимок камерой") },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        showAvatarSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }) {
                        Text("Галерея")
                    }
                    TextButton(onClick = {
                        showAvatarSourceDialog = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val uriString = viewModel.createCameraOutputUri()
                            uriString?.let { cameraLauncher.launch(Uri.parse(it)) }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("Камера")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarSourceDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun AvatarView(
    avatarUri: String,
    size: Int,
    onClick: (() -> Unit)? = null
) {

    val avatarModifier = Modifier
        .size(size.dp)
        .clip(CircleShape)
        .background(AvatarBlue)
        .then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )

    if (avatarUri.isBlank()) {
        Box(
            modifier = avatarModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Фото",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        AsyncImage(
            model = avatarUri,
            contentDescription = "Аватар",
            modifier = avatarModifier
        )
    }
}

@Composable
fun ProfileInfoCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value)
        }
    }
}

@Composable
fun RepoListItem(
    repo: GithubRepo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(AvatarBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = repo.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = repo.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Владелец: ${repo.owner}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = "Язык: ${repo.language} • ★ ${repo.stars}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Карточка") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBarBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(text = repo.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                Button(
                    onClick = {
                        if (isFavorite) favoritesViewModel.removeFromFavorites(repo.id)
                        else favoritesViewModel.addToFavorites(repo)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isFavorite) "Убрать из избранного" else "Добавить в избранное")
                }
            }
            item { InfoRow(label = "Владелец", value = repo.owner) }
            item { InfoRow(label = "Язык", value = repo.language) }
            item { InfoRow(label = "Звёзды", value = repo.stars.toString()) }
            item { InfoRow(label = "Форки", value = repo.forks.toString()) }
            item { InfoRow(label = "Открытые задачи", value = repo.openIssues.toString()) }
            item { InfoRow(label = "Ветка по умолчанию", value = repo.defaultBranch) }
            item {
                Text(text = "Описание", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = repo.description)
            }
            item {
                Text(text = "Ссылка", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = repo.url, color = LinkBlue)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}

private fun galleryPermissionForCurrentAndroid(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun openDownloadedFile(context: Context, file: DownloadedFile) {
    val uri = Uri.parse(file.uriString)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, file.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "Не найдено приложение для открытия файла ${file.fileName}", Toast.LENGTH_SHORT).show()
    }
}
