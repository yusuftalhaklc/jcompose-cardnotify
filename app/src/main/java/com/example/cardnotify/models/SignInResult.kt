package com.example.cardnotify.models

import java.util.Date

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

/*data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)*/

data class UserData (
    val uuid:String,
    val displayName:String?,
    val email:String?,
    val profilePictureUrl:String?,
    val gender:String? ="",
    val matchingUser:String?="",
    val premium:Boolean ?= false,
    val createdAt:String?="",
    val token:String? =""
) {  constructor() : this("", "", "", "", "", "", token = "")
}