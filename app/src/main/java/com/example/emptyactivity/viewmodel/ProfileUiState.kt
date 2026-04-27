package com.example.emptyactivity.viewmodel

import com.example.emptyactivity.domain.model.DownloadedFile
import com.example.emptyactivity.domain.model.UserProfile

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val editFullName: String = "",
    val editAvatarUri: String = "",
    val editResumeUrl: String = "",
    val editPosition: String = "",
    val editFavoriteClassTime: String = "",
    val favoriteTimeError: String? = null,
    val isDownloadingResume: Boolean = false
)

sealed interface ProfileEvent {
    data class OpenDownloadedFile(val file: DownloadedFile) : ProfileEvent
    data class ShowMessage(val message: String) : ProfileEvent
}
