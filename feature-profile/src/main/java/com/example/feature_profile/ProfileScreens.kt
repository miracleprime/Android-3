package com.example.feature_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val AppBarBlue = Color(0xFF3F51B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileScreenUiState,
    onEditClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                actions = {
                    TextButton(onClick = onEditClick) {
                        Text(stringResource(R.string.edit_button), color = Color.White)
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
            AvatarView(avatarUri = state.avatarUri, size = 120)

            if (state.isEmpty) {
                Text(stringResource(R.string.profile_empty))
            } else {
                Text(
                    text = if (state.fullName.isBlank()) {
                        stringResource(R.string.full_name_not_filled)
                    } else {
                        state.fullName
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (state.position.isNotBlank()) {
                    Text(state.position, color = Color.Gray)
                }

                ProfileInfoCard(
                    title = stringResource(R.string.favorite_pair_title),
                    value = if (state.favoriteClassTime.isBlank()) {
                        stringResource(R.string.favorite_pair_not_set)
                    } else {
                        state.favoriteClassTime
                    }
                )

                ProfileInfoCard(
                    title = stringResource(R.string.resume_link_title),
                    value = if (state.resumeUrl.isBlank()) {
                        stringResource(R.string.resume_not_set)
                    } else {
                        state.resumeUrl
                    }
                )

                Button(
                    onClick = onResumeClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.isDownloadingResume) {
                            stringResource(R.string.resume_loading)
                        } else {
                            stringResource(R.string.resume_button)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onBackClick: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onResumeUrlChange: (String) -> Unit,
    onFavoriteClassTimeChange: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onOpenTimePicker: () -> Unit,
    onDoneClick: () -> Unit,
    showAvatarSourceDialog: Boolean,
    onDismissAvatarDialog: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
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
                title = { Text(stringResource(R.string.edit_profile_title)) },
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
                avatarUri = state.avatarUri,
                size = 120,
                onClick = onAvatarClick
            )

            Text(stringResource(R.string.choose_avatar_hint))

            OutlinedTextField(
                value = state.fullName,
                onValueChange = onFullNameChange,
                label = { Text(stringResource(R.string.full_name_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.position,
                onValueChange = onPositionChange,
                label = { Text(stringResource(R.string.position_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.resumeUrl,
                onValueChange = onResumeUrlChange,
                label = { Text(stringResource(R.string.resume_url_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.favoriteClassTime,
                onValueChange = onFavoriteClassTimeChange,
                label = { Text(stringResource(R.string.favorite_pair_time_label)) },
                placeholder = { Text(stringResource(R.string.time_placeholder)) },
                singleLine = true,
                isError = state.favoriteTimeError != null,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = onOpenTimePicker) {
                        Text(stringResource(R.string.time_picker_icon))
                    }
                }
            )

            state.favoriteTimeError?.let { errorText ->
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Button(
                onClick = onDoneClick,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.done))
            }
        }
    }

    if (showAvatarSourceDialog) {
        AlertDialog(
            onDismissRequest = onDismissAvatarDialog,
            title = { Text(stringResource(R.string.choose_avatar_title)) },
            text = { Text(stringResource(R.string.choose_avatar_text)) },
            confirmButton = {
                Column {
                    TextButton(onClick = onGalleryClick) {
                        Text(stringResource(R.string.gallery))
                    }
                    TextButton(onClick = onCameraClick) {
                        Text(stringResource(R.string.camera))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAvatarDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}