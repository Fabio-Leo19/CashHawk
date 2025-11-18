package com.fabio.cashcontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fabio.cashcontrol.data.TransactionRepositoryRoom

class AppViewModelFactory(
    private val repo: TransactionRepositoryRoom
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(repo) as T
    }
}
