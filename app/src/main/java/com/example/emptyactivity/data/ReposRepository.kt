package com.example.emptyactivity.data

import com.example.emptyactivity.model.GithubRepo

object ReposRepository {

    private val repos = listOf(
        GithubRepo(
            id = 1,
            name = "compose-catalog",
            owner = "mobile-lab",
            language = "Kotlin",
            stars = 1240,
            forks = 180,
            watchers = 95,
            openIssues = 12,
            url = "https://github.com/mobile-lab/compose-catalog",
            description = "Каталог UI-компонентов на Jetpack Compose.",
            defaultBranch = "main",
            visibility = "public"
        ),
        GithubRepo(
            id = 2,
            name = "notes-api",
            owner = "student-team",
            language = "Kotlin",
            stars = 860,
            forks = 96,
            watchers = 44,
            openIssues = 7,
            url = "https://github.com/student-team/notes-api",
            description = "Серверное API для заметок и авторизации.",
            defaultBranch = "main",
            visibility = "public"
        ),
        GithubRepo(
            id = 3,
            name = "weather-dashboard",
            owner = "android-group",
            language = "Java",
            stars = 530,
            forks = 72,
            watchers = 31,
            openIssues = 4,
            url = "https://github.com/android-group/weather-dashboard",
            description = "Приложение с прогнозом погоды и аналитикой.",
            defaultBranch = "develop",
            visibility = "public"
        ),
        GithubRepo(
            id = 4,
            name = "design-system",
            owner = "frontend-core",
            language = "TypeScript",
            stars = 2100,
            forks = 320,
            watchers = 150,
            openIssues = 18,
            url = "https://github.com/frontend-core/design-system",
            description = "Набор UI-компонентов и токенов дизайна.",
            defaultBranch = "main",
            visibility = "public"
        ),
        GithubRepo(
            id = 5,
            name = "ml-experiments",
            owner = "data-labs",
            language = "Python",
            stars = 980,
            forks = 140,
            watchers = 67,
            openIssues = 9,
            url = "https://github.com/data-labs/ml-experiments",
            description = "Эксперименты с моделями и обработкой данных.",
            defaultBranch = "master",
            visibility = "public"
        )
    )

    fun getRepos(): List<GithubRepo> = repos

    fun getRepoById(id: Int): GithubRepo? = repos.find { it.id == id }
}
