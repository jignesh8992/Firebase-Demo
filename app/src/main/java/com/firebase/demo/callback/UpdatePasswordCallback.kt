package com.firebase.demo.callback

/**
 * UpdatePasswordCallback.kt - .
 * @author:  Jignesh N Patel
 * @date: 23-Jan-2021 6:33 PM
 */
interface UpdatePasswordCallback {
    fun onUpdatePasswordSuccess()
    fun onUpdatePasswordFailure(errorMessage: String)
}