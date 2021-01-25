package com.firebase.demo.callback

interface PasswordResetCallback {
    fun onPasswordResetSuccess()
    fun onPasswordResetFailure(errorMessage: String)
}