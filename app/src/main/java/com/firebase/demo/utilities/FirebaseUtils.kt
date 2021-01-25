package com.firebase.demo.utilities

import android.content.Context
import android.util.Log
import com.firebase.demo.R
import com.firebase.demo.callback.CreateUserCallback
import com.firebase.demo.callback.LoginCallback
import com.firebase.demo.callback.PasswordResetCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private const val TAG = "FirebaseUtils"

// [START getAuth]
/**
 * TODO.. Instance of FirebaseAuth
 *
 * @return
 */
fun getAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
}
// [START getAuth]


// [START getUser]
/**
 * TODO.. Instance of current user
 *
 * @return
 */
fun getUser(): FirebaseUser? {
    return getAuth().currentUser
}
// [END getUser]

// [START getUId]
/**
 * TODO.. uid of current user
 *
 * @return
 */
fun getUId(): String {
    return getUser()!!.uid
}
// [END getUId]

// [START getProvider]
/**
 * TODO.. Provider of current logged in user
 *
 * @return
 */
fun getProvider(): String? {
    val currentUser = FirebaseAuth.getInstance().currentUser
    currentUser?.let {
        return currentUser.providerData[1]!!.providerId
    }
    return null
}
// [END getProvider]

// [START sendEmailVerification]
/**
 * TODO.. Send email verification link
 *
 * @param loginCallback
 */
fun Context.sendEmailVerification(loginCallback: LoginCallback) {

    Log.i(TAG, Throwable().stackTrace[0].methodName)

    val user = getUser()
    if (user != null && user.isEmailVerified) {
        loginCallback.onLoginSuccess()
    } else if (user != null && !user.isEmailVerified) {
        user.sendEmailVerification().addOnCompleteListener { verificationTask ->
            if (verificationTask.isSuccessful) {
                Log.i(TAG, getString(R.string.email_sent))
                loginCallback.onLoginVerification()
            } else {
                val error = verificationTask.exception.toString()
                Log.e(TAG, error)
                loginCallback.onLoginFailure(error)
            }
        }
    }
}
// [END sendEmailVerification]

// [START createUser]
/**
 * TODO.. Create new user when user register
 *
 * @param email
 * @param password
 * @param createUserCallback
 */
fun createUser(email: String, password: String, createUserCallback: CreateUserCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    getAuth().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        when {
            task.isSuccessful -> {
                createUserCallback.onUserCreateSuccess()
            }
            else -> {
                val error = task.exception.toString()
                Log.e(TAG, error)
                createUserCallback.onUserCreateFailure(error)
            }
        }
    }
}
// [END createUser]

// [START signInUser]
/**
 * TODO.. Sign in user
 *
 * @param email
 * @param password
 * @param loginCallback
 */
fun Context.signInUser(email: String, password: String, loginCallback: LoginCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    getAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
        when {
            signInTask.isSuccessful -> {
                val currUser = getUser()
                if (currUser != null && currUser.isEmailVerified) {
                    loginCallback.onLoginSuccess()
                } else if (currUser != null && !currUser.isEmailVerified) {
                    sendEmailVerification(loginCallback)
                }
            }
            else -> {
                val error = signInTask.exception.toString()
                Log.e(TAG, error)
                loginCallback.onLoginFailure(error)
            }
        }
    }
}
// [END signInUser]


// [START sendPasswordResetEmail]
/**
 * TODO.. Send password reset link
 *
 * @param passwordResetCallback
 */
fun sendPasswordResetEmail(passwordResetCallback: PasswordResetCallback) {
    getAuth().sendPasswordResetEmail(getUser()!!.email!!)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Email sent.")
                passwordResetCallback.onPasswordResetSuccess()
            } else {
                val error = task.exception!!.localizedMessage.toString()
                Log.e(TAG, error)
                passwordResetCallback.onPasswordResetFailure(error)
            }
        }
}
// [END sendPasswordResetEmail]
