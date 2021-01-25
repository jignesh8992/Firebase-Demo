package com.firebase.demo.callback

interface CreateUserCallback {
    fun onUserCreateSuccess()
    fun onUserCreateFailure(errorMessage: String)
}