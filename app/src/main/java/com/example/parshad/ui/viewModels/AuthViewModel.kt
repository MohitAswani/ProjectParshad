package com.example.parshad.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parshad.MyApplication
import com.example.parshad.data.entities.User
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.util.Constants
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthViewModel(
    private val application: MyApplication,
    private val database: AuthDatabase
) : ViewModel() {

    private val userPreferences = application.userPreferences
    val isSignedIn = MutableLiveData<Boolean>()

    fun getIsSignedInFromDataStore() {
        viewModelScope.launch {
            userPreferences.getBooleanFromDataStore(Constants.KEY_IS_SIGNED_IN).catch { e ->
                e.printStackTrace()
            }.collect {
                isSignedIn.postValue(it)
            }
        }
    }

    fun setIsSignedInFromDataStore(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveBooleanToDataStore(Constants.KEY_IS_SIGNED_IN, value)
        }
    }

    fun saveUserData(user: User) {
        viewModelScope.launch {
            userPreferences.saveToDataStore(Constants.USER_NAME, user.name)
        }
        viewModelScope.launch {
            userPreferences.saveToDataStore(
                Constants.USER_PHONE_NUMBER,
                user.phoneNumber
            )
        }
        viewModelScope.launch {
            userPreferences.saveToDataStore(
                Constants.USER_AADHAR,
                user.aadhar
            )
        }
        viewModelScope.launch {
            userPreferences.saveToDataStore(
                Constants.USER_ADDRESS,
                user.currentAddress
            )
        }
        viewModelScope.launch {
            userPreferences.saveToDataStore(
                Constants.USER_GENDER,
                user.gender
            )
        }
        viewModelScope.launch { userPreferences.saveToDataStore(Constants.USER_IMAGE, user.image) }
        viewModelScope.launch {
            userPreferences.saveToDataStore(
                Constants.USER_ROLE,
                user.userRole
            )
        }
    }
}