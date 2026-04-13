package com.example.emptyactivity.domain.model

data class UserProfile(
    val fullName: String = "",
    val avatarUri: String = "",
    val resumeUrl: String = "",
    val position: String = ""
) {
    fun isEmpty(): Boolean {
        return fullName.isBlank() && avatarUri.isBlank() && resumeUrl.isBlank() && position.isBlank()
    }
}
