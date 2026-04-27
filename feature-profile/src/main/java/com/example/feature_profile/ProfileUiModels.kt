package com.example.feature_profile

data class ProfileScreenUiState(
    val fullName: String,
    val position: String,
    val avatarUri: String,
    val resumeUrl: String,
    val favoriteClassTime: String,
    val isEmpty: Boolean,
    val isDownloadingResume: Boolean
)

data class EditProfileUiState(
    val fullName: String,
    val position: String,
    val avatarUri: String,
    val resumeUrl: String,
    val favoriteClassTime: String,
    val favoriteTimeError: String?,
    val canSave: Boolean
)