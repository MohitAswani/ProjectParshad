package com.example.parshad.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.parshad.MyApplication
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.ui.viewModels.AuthViewModel
import com.example.parshad.ui.viewModels.AuthViewModelFactory
import com.example.parshadapp.R

class AuthActivity : AppCompatActivity() {
    lateinit var authDatabase: AuthDatabase
    lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        authDatabase = AuthDatabase()
        val authViewModelFactory = AuthViewModelFactory(application as MyApplication, authDatabase)
        authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]
        authViewModel.getIsSignedInFromDataStore()
        setContentView(R.layout.activity_auth)
    }


}