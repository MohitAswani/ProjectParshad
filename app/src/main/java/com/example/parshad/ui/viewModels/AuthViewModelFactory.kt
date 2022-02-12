package com.example.parshad.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parshad.MyApplication
import com.example.parshad.data.remote.AuthDatabase

class AuthViewModelFactory(
    private val application: MyApplication,
    private val database: AuthDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(application,database) as T
    }

}