package com.firebase.demo.sociallogin.callback

import com.firebase.demo.sociallogin.dao.UserProfile

interface SignInCallback {
    fun onLoginSuccess(userProfile: UserProfile?)
    fun onLoginFailure(errorMessage: String?)
}