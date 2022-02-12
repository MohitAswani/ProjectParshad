package com.example.parshad.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parshad.MyApplication
import com.example.parshad.data.remote.AuthDatabase

class MainViewModelFactory(
    private val application: MyApplication,
    private val database: AuthDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(application,database) as T
    }

}