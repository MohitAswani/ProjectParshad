package com.example.parshad

import android.app.Application
import com.example.parshad.util.UserPreferences

class MyApplication : Application(){

    lateinit var userPreferences: UserPreferences

    override fun onCreate() {
        super.onCreate()
        userPreferences=UserPreferences(this)
    }
}