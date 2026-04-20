package com.example.emptyactivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emptyactivity.data.notifications.FavoritePairAlarmScheduler
import com.example.emptyactivity.domain.model.UserProfile
import com.example.emptyactivity.domain.repository.UserProfileRepository
import com.example.emptyactivity.domain.usecase.DownloadResumeFileUseCase
import com.example.emptyactivity.domain.usecase.ObserveUserProfileUseCase
import com.example.emptyactivity.domain.usecase.SaveUserProfileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

class ProfileViewModel(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val downloadResumeFileUseCase: DownloadResumeFileUseCase,
    private val userProfileRepository: UserProfileRepository,
    private val favoritePairAlarmScheduler: FavoritePairAlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    private var pendingCameraUriString: String? = null

    init {
        viewModelScope.launch {
            observeUserProfileUseCase().collectLatest { profile ->
                _uiState.update { current -> current.copy(profile = profile) }
            }
        }
    }

    fun startEditing() {
        val profile = _uiState.value.profile
        _uiState.update {
            it.copy(
                editFullName = profile.fullName,
                editAvatarUri = profile.avatarUri,
                editResumeUrl = profile.resumeUrl,
                editPosition = profile.position,
                editFavoriteClassTime = profile.favoriteClassTime,
                favoriteTimeError = validateFavoriteClassTime(profile.favoriteClassTime)
            )
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(editFullName = value) }
    }

    fun onPositionChange(value: String) {
        _uiState.update { it.copy(editPosition = value) }
    }

    fun onResumeUrlChange(value: String) {
        _uiState.update { it.copy(editResumeUrl = value) }
    }

    fun onFavoriteClassTimeChange(value: String) {
        _uiState.update {
            it.copy(
                editFavoriteClassTime = value,
                favoriteTimeError = validateFavoriteClassTime(value)
            )
        }
    }

    fun onFavoriteClassTimePicked(hour: Int, minute: Int) {
        val formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        _uiState.update {
            it.copy(
                editFavoriteClassTime = formatted,
                favoriteTimeError = null
            )
        }
    }

    fun saveProfile() {
        if (!canSaveProfile()) return

        viewModelScope.launch {
            val newProfile = UserProfile(
                fullName = _uiState.value.editFullName.trim(),
                avatarUri = _uiState.value.editAvatarUri,
                resumeUrl = _uiState.value.editResumeUrl.trim(),
                position = _uiState.value.editPosition.trim(),
                favoriteClassTime = _uiState.value.editFavoriteClassTime.trim()
            )

            saveUserProfileUseCase(newProfile)
            favoritePairAlarmScheduler.schedule(newProfile.fullName, newProfile.favoriteClassTime)
        }
    }

    fun canSaveProfile(): Boolean {
        val time = _uiState.value.editFavoriteClassTime
        return time.isNotBlank() && validateFavoriteClassTime(time) == null
    }

    private fun validateFavoriteClassTime(value: String): String? {
        if (value.isBlank()) return "Укажи время в формате HH:mm"

        return try {
            val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
            parser.isLenient = false
            parser.parse(value) ?: return "Время должно быть в формате HH:mm"
            null
        } catch (_: Exception) {
            "Время должно быть в формате HH:mm"
        }
    }

    fun createCameraOutputUri(): String? {
        return userProfileRepository.createCameraOutputUri()
            .onSuccess { uriString -> pendingCameraUriString = uriString }
            .onFailure {
                viewModelScope.launch {
                    _events.emit(ProfileEvent.ShowMessage("Не удалось подготовить фото для камеры"))
                }
            }
            .getOrNull()
    }

    fun onCameraCaptureResult(success: Boolean) {
        if (success) {
            pendingCameraUriString?.let { savedUri ->
                _uiState.update { it.copy(editAvatarUri = savedUri) }
            }
        }
    }

    fun onGalleryImageChosen(sourceUriString: String) {
        viewModelScope.launch {
            userProfileRepository.saveAvatarFromGallery(sourceUriString)
                .onSuccess { savedUri ->
                    _uiState.update { it.copy(editAvatarUri = savedUri) }
                }
                .onFailure {
                    _events.emit(ProfileEvent.ShowMessage("Не удалось сохранить фото из галереи"))
                }
        }
    }

    fun openResume() {
        val resumeUrl = _uiState.value.profile.resumeUrl
        if (resumeUrl.isBlank()) {
            viewModelScope.launch { _events.emit(ProfileEvent.ShowMessage("Ссылка на резюме пока не заполнена")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingResume = true) }

            downloadResumeFileUseCase(resumeUrl)
                .onSuccess { downloadedFile -> _events.emit(ProfileEvent.OpenDownloadedFile(downloadedFile)) }
                .onFailure { _events.emit(ProfileEvent.ShowMessage("Не удалось скачать файл резюме")) }

            _uiState.update { it.copy(isDownloadingResume = false) }
        }
    }

    companion object {
        fun factory(
            observeUserProfileUseCase: ObserveUserProfileUseCase,
            saveUserProfileUseCase: SaveUserProfileUseCase,
            downloadResumeFileUseCase: DownloadResumeFileUseCase,
            userProfileRepository: UserProfileRepository,
            favoritePairAlarmScheduler: FavoritePairAlarmScheduler
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        observeUserProfileUseCase = observeUserProfileUseCase,
                        saveUserProfileUseCase = saveUserProfileUseCase,
                        downloadResumeFileUseCase = downloadResumeFileUseCase,
                        userProfileRepository = userProfileRepository,
                        favoritePairAlarmScheduler = favoritePairAlarmScheduler
                    ) as T
                }
            }
        }
    }
}
