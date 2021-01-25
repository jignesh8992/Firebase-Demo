package com.firebase.demo.callback

interface LoginCallback {
    fun onLoginSuccess()
    fun onLoginVerification()
    fun onLoginFailure(errorMessage: String)
}