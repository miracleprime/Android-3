package com.example.emptyactivity.domain.usecase

import com.example.emptyactivity.domain.model.UserProfile
import com.example.emptyactivity.domain.repository.UserProfileRepository

class SaveUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) {
        repository.saveProfile(profile)
    }
}
