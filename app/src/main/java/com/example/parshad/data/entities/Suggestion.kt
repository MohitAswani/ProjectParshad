package com.example.parshad.data.entities

import java.util.*

data class Suggestion(
    val posterImage:String,
    val posterName:String,
    val posterNumber:String,
    val posterWard:String,
    val description:String,
    val attachment:String?,
    val attachmentName:String?,
    val attachmentType: String,
    val suggestionLikes:Long,
    val postedDate:Date,
    val postedTime:Long
)