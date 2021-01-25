package com.firebase.demo.callback

import com.firebase.demo.sociallogin.dao.UserProfile

interface AuthUserCallback {
    fun onAuthUserSuccess(userProfile: UserProfile)
    fun onAuthUserFailure(errorMessage: String)
}