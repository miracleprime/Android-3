package com.example.emptyactivity.domain.repository

import com.example.emptyactivity.domain.model.DownloadedFile
import com.example.emptyactivity.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun saveProfile(profile: UserProfile)

    suspend fun saveAvatarFromGallery(sourceUriString: String): Result<String>

    fun createCameraOutputUri(): Result<String>

    suspend fun downloadResume(resumeUrl: String): Result<DownloadedFile>
}
