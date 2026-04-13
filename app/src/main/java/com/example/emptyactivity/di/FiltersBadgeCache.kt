package com.example.emptyactivity.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FiltersBadgeCache {
    private val _shouldShowBadge = MutableStateFlow(false)
    val shouldShowBadge: StateFlow<Boolean> = _shouldShowBadge.asStateFlow()

    fun setShouldShowBadge(value: Boolean) {
        _shouldShowBadge.value = value
    }
}