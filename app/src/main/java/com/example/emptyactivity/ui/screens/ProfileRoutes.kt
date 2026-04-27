package com.example.emptyactivity.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emptyactivity.R
import com.example.emptyactivity.domain.model.DownloadedFile
import com.example.emptyactivity.viewmodel.ProfileEvent
import com.example.emptyactivity.viewmodel.ProfileViewModel
import com.example.feature_profile.EditProfileScreen as FeatureEditProfileScreen
import com.example.feature_profile.EditProfileUiState
import com.example.feature_profile.ProfileScreen as FeatureProfileScreen
import com.example.feature_profile.ProfileScreenUiState
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@Composable
fun ProfileRoute(
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

    FeatureProfileScreen(
        state = ProfileScreenUiState(
            fullName = uiState.profile.fullName,
            position = uiState.profile.position,
            avatarUri = uiState.profile.avatarUri,
            resumeUrl = uiState.profile.resumeUrl,
            favoriteClassTime = uiState.profile.favoriteClassTime,
            isEmpty = uiState.profile.isEmpty(),
            isDownloadingResume = uiState.isDownloadingResume
        ),
        onEditClick = onEditClick,
        onResumeClick = { viewModel.openResume() }
    )
}

@Composable
fun EditProfileRoute(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val storagePermissionDeniedText = stringResource(R.string.storage_permission_denied)
    val cameraPermissionDeniedText = stringResource(R.string.camera_permission_denied)
    val notificationPermissionDeniedText = stringResource(R.string.notification_permission_denied)
    val exactAlarmPermissionText = stringResource(R.string.exact_alarm_permission_needed)

    var showAvatarSourceDialog by remember { mutableStateOf(false) }
    var storagePermissionAsked by rememberSaveable { mutableStateOf(false) }

    val galleryPermission = galleryPermissionForCurrentAndroid()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onGalleryImageChosen(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onCameraCaptureResult(success)
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, storagePermissionDeniedText, Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uriString = viewModel.createCameraOutputUri()
            uriString?.let { cameraLauncher.launch(Uri.parse(it)) }
        } else {
            Toast.makeText(context, cameraPermissionDeniedText, Toast.LENGTH_SHORT).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, notificationPermissionDeniedText, Toast.LENGTH_LONG).show()
        }
        onDoneClick()
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

    val openTimePicker: () -> Unit = {
        val calendar = Calendar.getInstance()
        val currentText = uiState.editFavoriteClassTime

        val hour: Int
        val minute: Int

        if (currentText.matches(Regex("\\d{2}:\\d{2}"))) {
            hour = currentText.substringBefore(":").toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
            minute = currentText.substringAfter(":").toIntOrNull() ?: calendar.get(Calendar.MINUTE)
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                viewModel.onFavoriteClassTimePicked(selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        ).show()
    }

    val saveProfileAndClose: () -> Unit = {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(context, exactAlarmPermissionText, Toast.LENGTH_LONG).show()

            context.startActivity(
                Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onDoneClick()
        }
    }

    FeatureEditProfileScreen(
        state = EditProfileUiState(
            fullName = uiState.editFullName,
            position = uiState.editPosition,
            avatarUri = uiState.editAvatarUri,
            resumeUrl = uiState.editResumeUrl,
            favoriteClassTime = uiState.editFavoriteClassTime,
            favoriteTimeError = uiState.favoriteTimeError,
            canSave = viewModel.canSaveProfile()
        ),
        onBackClick = onBackClick,
        onFullNameChange = viewModel::onFullNameChange,
        onPositionChange = viewModel::onPositionChange,
        onResumeUrlChange = viewModel::onResumeUrlChange,
        onFavoriteClassTimeChange = viewModel::onFavoriteClassTimeChange,
        onAvatarClick = { showAvatarSourceDialog = true },
        onOpenTimePicker = openTimePicker,
        onDoneClick = saveProfileAndClose,
        showAvatarSourceDialog = showAvatarSourceDialog,
        onDismissAvatarDialog = { showAvatarSourceDialog = false },
        onGalleryClick = {
            showAvatarSourceDialog = false
            galleryLauncher.launch("image/*")
        },
        onCameraClick = {
            showAvatarSourceDialog = false
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val uriString = viewModel.createCameraOutputUri()
                uriString?.let { cameraLauncher.launch(Uri.parse(it)) }
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    )
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
        Toast.makeText(
            context,
            context.getString(R.string.resume_open_error, file.fileName),
            Toast.LENGTH_SHORT
        ).show()
    }
}