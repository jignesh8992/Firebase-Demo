package com.firebase.demo.callback

/**
 * UserReAuthenticateCallback.kt - .
 * @author:  Jignesh N Patel
 * @date: 23-Jan-2021 6:29 PM
 */
interface UserReAuthenticateCallback {
    fun onUserReAuthenticateSuccess()
    fun onUserReAuthenticateFailure(errorMessage: String)
}