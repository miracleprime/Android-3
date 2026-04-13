package com.example.emptyactivity.domain.usecase

import com.example.emptyactivity.domain.model.UserProfile
import com.example.emptyactivity.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.observeProfile()
}
