package com.example.emptyactivity.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emptyactivity.data.ReposRepository
import com.example.emptyactivity.model.GithubRepo

class ReposViewModel : ViewModel() {

    val repos: List<GithubRepo> = ReposRepository.getRepos()

    fun getRepoById(id: Int): GithubRepo? {
        return ReposRepository.getRepoById(id)
    }
}
