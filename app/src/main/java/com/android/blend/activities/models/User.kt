package com.android.blend.activities.models

class User(val uid: String ,val phoneNumber: String ,val username: String,  val profileImageUrl: String){
    constructor() : this("", "", "", "")
}