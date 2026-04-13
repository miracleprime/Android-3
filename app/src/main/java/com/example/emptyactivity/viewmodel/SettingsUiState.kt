package com.example.emptyactivity.viewmodel

data class SettingsUiState(
    val username: String = "",
    val repoNameQuery: String = "",
    val minStarsText: String = "0"
)