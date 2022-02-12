package com.example.parshad.data.entities

import java.util.*

data class Problems(
    val posterImage:String,
    val posterName:String,
    val posterNumber:String,
    val posterWard:String,
    val description:String,
    val severity:Long=0L,
    val attachment:String?,
    val attachmentName:String?,
    val attachmentType: String,
    val postedDate:Date,
    val postedTime:Long
)