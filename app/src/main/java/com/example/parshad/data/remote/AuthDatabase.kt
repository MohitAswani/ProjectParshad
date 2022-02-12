package com.example.parshad.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AuthDatabase {

    private val _auth:FirebaseAuth= FirebaseAuth.getInstance()
    val auth get()=_auth

    private val _firestore=FirebaseFirestore.getInstance()
    val firestore get()=_firestore

    private val _storage=Firebase.storage
    val storage get()=_storage

}