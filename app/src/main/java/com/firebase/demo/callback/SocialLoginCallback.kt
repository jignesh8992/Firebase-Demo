package com.firebase.demo.callback

import com.firebase.demo.sociallogin.dao.UserProfile

interface SocialLoginCallback {
    fun onSocialLoginSuccess(userProfile: UserProfile?)
    fun onSocialLoginFailure(errorMessage: String?)
}