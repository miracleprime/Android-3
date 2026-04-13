package com.example.emptyactivity.domain.usecase

import com.example.emptyactivity.domain.model.DownloadedFile
import com.example.emptyactivity.domain.repository.UserProfileRepository

class DownloadResumeFileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(resumeUrl: String): Result<DownloadedFile> {
        return repository.downloadResume(resumeUrl)
    }
}
