package com.example.parshad.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.parshad.util.Constants
import kotlinx.coroutines.flow.map

class UserPreferences(val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.USER_PREFERENCES)

    suspend fun saveToDataStore(key: String, value: String) {
        val tempKey = stringPreferencesKey(key)
        context.dataStore.edit {
            it[tempKey] = value
        }
    }

    suspend fun saveBooleanToDataStore(key: String, value: Boolean) {
        val tempKey = booleanPreferencesKey(key)
        context.dataStore.edit {
            it[tempKey] = value
            Log.d("AuthViewModel", value.toString())
        }
    }

    fun getFromDataStore(key: String) = context.dataStore.data.map {
        val tempKey = stringPreferencesKey(key)
        it[tempKey]
    }

    fun getBooleanFromDataStore(key: String) = context.dataStore.data.map {
        val tempKey = booleanPreferencesKey(key)
        it[tempKey]
    }


}