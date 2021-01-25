package com.firebase.demo.callback

import com.firebase.demo.sociallogin.dao.UserProfile

interface AddUserCallback {
    fun onUserAddedSuccess(userProfile: UserProfile)
    fun onUserExist(userProfile: UserProfile)
    fun onUserAddFailure(errorMessage: String)
}