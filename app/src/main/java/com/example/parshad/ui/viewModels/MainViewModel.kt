package com.example.parshad.ui.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parshad.MyApplication
import com.example.parshad.data.entities.Problems
import com.example.parshad.data.entities.Suggestion
import com.example.parshad.data.entities.User
import com.example.parshad.data.remote.AuthDatabase
import com.example.parshad.util.Constants
import com.example.parshad.util.Resource
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val application: MyApplication,
    private val database: AuthDatabase
) : ViewModel() {

    private val userPreferences = application.userPreferences

    val userName = MutableLiveData<String>()
    val userImage = MutableLiveData<String>()
    val userPhone = MutableLiveData<String>()
    val userAadhar = MutableLiveData<String>()
    val userWard = MutableLiveData<String>()
    val userRole = MutableLiveData<String>()
    val userGender = MutableLiveData<String>()
    val problems = MutableLiveData<Resource<MutableList<Problems>>>()
    val myproblems = MutableLiveData<Resource<MutableList<Problems>>>()
    val suggestions = MutableLiveData<Resource<MutableList<Suggestion>>>()
    val accomplished = MutableLiveData<Resource<MutableList<Problems>>>()

    fun setIsSignedInFromDataStore(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveBooleanToDataStore(Constants.KEY_IS_SIGNED_IN, value)
        }
    }


    fun getNameFromDataStore() {
        viewModelScope.launch {
            userPreferences.getFromDataStore(Constants.USER_NAME).catch { e ->
                e.printStackTrace()
            }.collect {
                userName.postValue(it)
            }
        }
    }

    fun getUserImageFromDataStore() {
        viewModelScope.launch {
            userPreferences.getFromDataStore(Constants.USER_IMAGE).catch { e ->
                e.printStackTrace()
            }.collect {
                userImage.postValue(it)
                if (it != null) {
                    Log.d("MainVM", it)
                }
            }
        }
    }

    fun getUserWardFromDataStore() {
        viewModelScope.launch {
            userPreferences.getFromDataStore(Constants.USER_ADDRESS).catch { e ->
                e.printStackTrace()
            }.collect {
                userWard.postValue(it)
            }
        }
    }

    fun getUserGenderFromDataStore() {
        viewModelScope.launch {
            userPreferences.getFromDataStore(Constants.USER_ADDRESS).catch { e ->
                e.printStackTrace()
            }.collect {
                userGender.postValue(it)
            }
        }
    }

    fun getAadhaarFromDataStore() {
        viewModelScope.launch {
            userPreferences.getFromDataStore(Constants.USER_AADHAR).catch { e ->
                e.printStackTrace()
            }.collect {
                userAadhar.postValue(it)
            }
        }
    }

    fun getUserPhoneFromDataStore() {
        viewModelScope.launch {
            userPreferences.getFromDataStore(Constants.KEY_PHONE_NUMBER).catch { e ->
                e.printStackTrace()
            }.collect {
                userPhone.postValue(it)
            }
        }
    }

    fun getUserRoleFromDataStore() {
        viewModelScope.launch {
            userPreferences.getFromDataStore(Constants.USER_ROLE).catch { e ->
                e.printStackTrace()
            }.collect {
                userRole.postValue(it)
            }
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


    fun loadProblems() {
        problems.postValue(Resource.loading())
        try {
            getUserWardFromDataStore()
            problems.postValue(Resource.success(listenProblems(userWard.value ?: "")))
        } catch (t: Throwable) {
            Log.d("MainViewModel", t.toString())
        }
    }

    private fun listenProblems(problemArea: String): MutableList<Problems> {
        val problemsList = mutableListOf<Problems>()
        database.firestore.collection(Constants.KEY_PROBLEMS)
            .whereEqualTo(
                Constants.PROBLEM_POSTER_WARD,
                problemArea
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("AuthDatabase", e.toString())
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val problem = Problems(
                                    posterImage = dc.document.getString(Constants.PROBLEM_POSTER_IMAGE)
                                        ?: "",
                                    posterName = dc.document.getString(Constants.PROBLEM_POSTER_NAME)
                                        ?: "",
                                    posterNumber = dc.document.getString(Constants.PROBLEM_POSTER_NUMBER)
                                        ?: "",
                                    posterWard = dc.document.getString(Constants.PROBLEM_POSTER_WARD)
                                        ?: "",
                                    description = dc.document.getString(Constants.PROBLEM_DESCRIPTION)
                                        ?: "",
                                    severity = dc.document.getLong(Constants.PROBLEM_SEVERITY) ?: 0,
                                    attachment = dc.document.getString(Constants.PROBLEM_ATTACHMENT)
                                        ?: "",
                                    attachmentType = dc.document.getString(Constants.PROBLEM_ATTACHMENT_TYPE)
                                        ?: "",
                                    attachmentName = dc.document.getString(Constants.PROBLEM_ATTACHMENT_NAME),
                                    postedDate = dc.document.getDate(Constants.PROBLEM_DATE)
                                        ?: Date(),
                                    postedTime = dc.document.getLong(Constants.PROBLEM_TIME) ?: 0
                                )
                                problemsList.add(problem)
                            }
                            else -> {}
                        }
                    }
                }
                problemsList.sortByDescending {
                    it.severity
                }
                problems.postValue(Resource.success(problemsList))
            }

        Log.d("AuthDatabase", problemsList.size.toString())
        return problemsList
    }

    fun postProblems(problem: Problems) {
        database.firestore.collection(Constants.KEY_PROBLEMS)
            .add(problem)
    }

    fun deleteProblems(problem: Problems) {
        database.firestore.collection(Constants.KEY_PROBLEMS)
            .whereEqualTo(
                Constants.PROBLEM_DATE,
                problem.postedDate
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("AuthDatabase", e.toString())
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (dc in snapshots.documents) {
                        val problem = Problems(
                            posterImage = dc.getString(Constants.PROBLEM_POSTER_IMAGE) ?: "",
                            posterName = dc.getString(Constants.PROBLEM_POSTER_NAME) ?: "",
                            posterNumber = dc.getString(Constants.PROBLEM_POSTER_NUMBER)
                                ?: "",
                            posterWard = dc.getString(Constants.PROBLEM_POSTER_WARD) ?: "",
                            description = dc.getString(Constants.PROBLEM_DESCRIPTION)
                                ?: "",
                            severity = dc.getLong(Constants.PROBLEM_SEVERITY) ?: 0,
                            attachment = dc.getString(Constants.PROBLEM_ATTACHMENT) ?: "",
                            attachmentType = dc.getString(Constants.PROBLEM_ATTACHMENT_TYPE)
                                ?: "",
                            attachmentName = dc.getString(Constants.PROBLEM_ATTACHMENT_NAME),
                            postedDate = dc.getDate(Constants.PROBLEM_DATE) ?: Date(),
                            postedTime = dc.getLong(Constants.PROBLEM_TIME) ?: 0
                        )
                        postAccomplished(problem)
                        dc.reference.delete()
                    }
                }
            }

        problems.postValue(Resource.loading())
        loadProblems()
    }

    fun loadmyProblems() {
        problems.postValue(Resource.loading())
        try {
            getUserWardFromDataStore()
            listenmyProblems(userWard.value ?: "", userPhone.value ?: "")
        } catch (t: Throwable) {
            Log.d("MainViewModel", t.toString())
        }
    }

    private fun listenmyProblems(problemArea: String, phoneNumber: String): MutableList<Problems> {
        val problemsList = mutableListOf<Problems>()
        database.firestore.collection(Constants.KEY_PROBLEMS)
            .whereEqualTo(
                Constants.PROBLEM_POSTER_WARD,
                problemArea
            )
            .whereEqualTo(
                Constants.PROBLEM_POSTER_NUMBER,
                phoneNumber
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("AuthDatabase", e.toString())
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val problem = Problems(
                                    posterImage = dc.document.getString(Constants.PROBLEM_POSTER_IMAGE)
                                        ?: "",
                                    posterName = dc.document.getString(Constants.PROBLEM_POSTER_NAME)
                                        ?: "",
                                    posterNumber = dc.document.getString(Constants.PROBLEM_POSTER_NUMBER)
                                        ?: "",
                                    posterWard = dc.document.getString(Constants.PROBLEM_POSTER_WARD)
                                        ?: "",
                                    description = dc.document.getString(Constants.PROBLEM_DESCRIPTION)
                                        ?: "",
                                    severity = dc.document.getLong(Constants.PROBLEM_SEVERITY) ?: 0,
                                    attachment = dc.document.getString(Constants.PROBLEM_ATTACHMENT)
                                        ?: "",
                                    attachmentType = dc.document.getString(Constants.PROBLEM_ATTACHMENT_TYPE)
                                        ?: "",
                                    attachmentName = dc.document.getString(Constants.PROBLEM_ATTACHMENT_NAME),
                                    postedDate = dc.document.getDate(Constants.PROBLEM_DATE)
                                        ?: Date(),
                                    postedTime = dc.document.getLong(Constants.PROBLEM_TIME) ?: 0
                                )
                                problemsList.add(problem)
                            }
                            else -> {}
                        }
                    }
                }
                problemsList.sortByDescending {
                    it.severity
                }
                myproblems.postValue(Resource.success(problemsList))
            }

        Log.d("AuthDatabase", problemsList.size.toString())
        return problemsList
    }


    fun loadSuggestions() {
        problems.postValue(Resource.loading())
        try {
            getUserWardFromDataStore()
            suggestions.postValue(Resource.success(listenSuggestions(userWard.value ?: "")))
        } catch (t: Throwable) {
            Log.d("MainViewModel", t.toString())
        }
    }

    private fun listenSuggestions(suggestionArea: String): MutableList<Suggestion> {
        val suggestionList = mutableListOf<Suggestion>()
        database.firestore.collection(Constants.KEY_SUGGESTION)
            .whereEqualTo(
                Constants.PROBLEM_POSTER_WARD,
                suggestionArea
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("AuthDatabase", e.toString())
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val suggestion = Suggestion(
                                    posterImage = dc.document.getString(Constants.SUGGESTION_POSTER_IMAGE)
                                        ?: "",
                                    posterName = dc.document.getString(Constants.SUGGESTION_POSTER_NAME)
                                        ?: "",
                                    posterNumber = dc.document.getString(Constants.SUGGESTION_POSTER_NUMBER)
                                        ?: "",
                                    posterWard = dc.document.getString(Constants.SUGGESTION_POSTER_WARD)
                                        ?: "",
                                    description = dc.document.getString(Constants.SUGGESTION_DESCRIPTION)
                                        ?: "",
                                    attachment = dc.document.getString(Constants.SUGGESTION_ATTACHMENT)
                                        ?: "",
                                    attachmentType = dc.document.getString(Constants.SUGGESTION_ATTACHMENT_TYPE)
                                        ?: "",
                                    attachmentName = dc.document.getString(Constants.SUGGESTION_ATTACHMENT_NAME),
                                    postedDate = dc.document.getDate(Constants.SUGGESTION_DATE)
                                        ?: Date(),
                                    postedTime = dc.document.getLong(Constants.SUGGESTION_TIME)
                                        ?: 0,
                                    suggestionLikes = dc.document.getLong(Constants.SUGGESTION_LIKES)
                                        ?: 0,
                                )
                                suggestionList.add(suggestion)
                            }
                            else -> {}
                        }
                    }
                }
                suggestionList.sortByDescending {
                    it.postedDate
                }
                suggestions.postValue(Resource.success(suggestionList))
            }

        return suggestionList
    }

    fun postSuggestion(suggestion: Suggestion) {
        database.firestore.collection(Constants.KEY_SUGGESTION)
            .add(suggestion)
    }

    fun loadAccomplishments() {
        problems.postValue(Resource.loading())
        try {
            getUserWardFromDataStore()
            accomplished.postValue(Resource.success(listenAccomplishments(userWard.value ?: "")))
        } catch (t: Throwable) {
            Log.d("MainViewModel", t.toString())
        }
    }

    private fun listenAccomplishments(Area: String): MutableList<Problems> {
        val accomplishedList = mutableListOf<Problems>()
        database.firestore.collection(Constants.KEY_ACCOMPLISHED)
            .whereEqualTo(
                Constants.PROBLEM_POSTER_WARD,
                Area
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("AuthDatabase", e.toString())
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val problem = Problems(
                                    posterImage = dc.document.getString(Constants.SUGGESTION_POSTER_IMAGE)
                                        ?: "",
                                    posterName = dc.document.getString(Constants.SUGGESTION_POSTER_NAME)
                                        ?: "",
                                    posterNumber = dc.document.getString(Constants.SUGGESTION_POSTER_NUMBER)
                                        ?: "",
                                    posterWard = dc.document.getString(Constants.SUGGESTION_POSTER_WARD)
                                        ?: "",
                                    description = dc.document.getString(Constants.SUGGESTION_DESCRIPTION)
                                        ?: "",
                                    attachment = dc.document.getString(Constants.SUGGESTION_ATTACHMENT)
                                        ?: "",
                                    attachmentType = dc.document.getString(Constants.SUGGESTION_ATTACHMENT_TYPE)
                                        ?: "",
                                    attachmentName = dc.document.getString(Constants.SUGGESTION_ATTACHMENT_NAME),
                                    postedDate = dc.document.getDate(Constants.SUGGESTION_DATE)
                                        ?: Date(),
                                    postedTime = dc.document.getLong(Constants.SUGGESTION_TIME)
                                        ?: 0,
                                )
                                accomplishedList.add(problem)
                            }
                            else -> {}
                        }
                    }
                }
                accomplishedList.sortByDescending {
                    it.postedDate
                }
                accomplished.postValue(Resource.success(accomplishedList))
            }

        return accomplishedList
    }

    private fun postAccomplished(problem: Problems) {
        database.firestore.collection(Constants.KEY_ACCOMPLISHED)
            .add(problem)
    }

    fun changeSettings() {
        database.firestore
            .collection(Constants.KEY_COLLECTIONS_USER)
            .whereEqualTo(
                Constants.USER_PHONE_NUMBER,
                userPhone.value.toString()
            )
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("AuthDatabase", e.toString())
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    Log.d("MainViewModel",userPhone.value.toString())
                    Log.d("MainViewModel",snapshots.documents.toString())
                    for (dc in snapshots.documents) {
                        dc.reference.update(Constants.USER_NAME,userName)
                        dc.reference.update(Constants.USER_IMAGE,userImage)
                        dc.reference.update(Constants.USER_ADDRESS,userWard)
                        dc.reference.update(Constants.USER_AADHAR,userAadhar)
                    }
                }
            }
    }
}