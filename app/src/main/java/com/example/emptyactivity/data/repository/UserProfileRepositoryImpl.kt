package com.example.emptyactivity.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.emptyactivity.domain.model.DownloadedFile
import com.example.emptyactivity.domain.model.UserProfile
import com.example.emptyactivity.domain.repository.UserProfileRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.net.URLConnection

class UserProfileRepositoryImpl(
    private val context: Context
) : UserProfileRepository {

    private val gson = Gson()

    private val profileDir = File(context.filesDir, "profile").apply { mkdirs() }
    private val avatarDir = File(profileDir, "avatars").apply { mkdirs() }
    private val profileFile = File(profileDir, "user_profile.json")
    private val resumeDir = File(
        context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir,
        "profile_resume"
    ).apply { mkdirs() }

    private val profileFlow = MutableStateFlow(readProfileFromFile())

    override fun observeProfile(): Flow<UserProfile> = profileFlow.asStateFlow()

    override suspend fun saveProfile(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            profileFile.parentFile?.mkdirs()
            profileFile.writeText(gson.toJson(profile))
            profileFlow.value = profile
        }
    }

    override suspend fun saveAvatarFromGallery(sourceUriString: String): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val sourceUri = Uri.parse(sourceUriString)
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                    ?: error("Не удалось открыть выбранное изображение")

                val destinationFile = File(avatarDir, "gallery_avatar_${System.currentTimeMillis()}.jpg")
                inputStream.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                fileToContentUri(destinationFile).toString()
            }
        }
    }

    override fun createCameraOutputUri(): Result<String> {
        return runCatching {
            val file = File(avatarDir, "camera_avatar_${System.currentTimeMillis()}.jpg")
            file.parentFile?.mkdirs()
            file.createNewFile()
            fileToContentUri(file).toString()
        }
    }

    override suspend fun downloadResume(resumeUrl: String): Result<DownloadedFile> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val normalizedUrl = normalizeResumeUrl(resumeUrl)
                val connection = URL(normalizedUrl).openConnection()
                connection.connect()

                val mimeType = connection.contentType ?: guessMimeTypeFromUrl(normalizedUrl)
                val extension = guessExtension(normalizedUrl, mimeType)
                val fileName = "resume_${System.currentTimeMillis()}.$extension"
                val targetFile = File(resumeDir, fileName)

                connection.getInputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                DownloadedFile(
                    uriString = fileToContentUri(targetFile).toString(),
                    mimeType = mimeType,
                    fileName = fileName
                )
            }
        }
    }

    private fun readProfileFromFile(): UserProfile {
        return runCatching {
            if (!profileFile.exists()) {
                UserProfile()
            } else {
                gson.fromJson(profileFile.readText(), UserProfile::class.java) ?: UserProfile()
            }
        }.getOrDefault(UserProfile())
    }

    private fun fileToContentUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun normalizeResumeUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
    }

    private fun guessExtension(url: String, mimeType: String): String {
        val cleanUrl = url.substringBefore('?')
        val fromUrl = cleanUrl.substringAfterLast('.', missingDelimiterValue = "")
        if (fromUrl.isNotBlank() && fromUrl.length <= 5) return fromUrl

        return when {
            mimeType.contains("pdf", ignoreCase = true) -> "pdf"
            mimeType.contains("msword", ignoreCase = true) -> "doc"
            mimeType.contains("wordprocessingml", ignoreCase = true) -> "docx"
            else -> "bin"
        }
    }

    private fun guessMimeTypeFromUrl(url: String): String {
        return URLConnection.guessContentTypeFromName(url) ?: "application/octet-stream"
    }
}
