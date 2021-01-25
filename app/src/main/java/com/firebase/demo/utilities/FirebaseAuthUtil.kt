package com.firebase.demo.utilities

import android.app.Activity
import android.util.Log
import com.facebook.AccessToken
import com.firebase.demo.R
import com.firebase.demo.callback.*
import com.firebase.demo.sociallogin.dao.UserProfile
import com.firebase.demo.sociallogin.getFBCallback
import com.firebase.demo.sociallogin.getGProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider

/**
 * FirebaseAuthUtil - Authenticate user with firebase.
 * @author:  Jignesh N Patel
 * @date: 23-Jan-2021 10:14 AM
 */

private const val TAG = "FirebaseAuthUtil"

// [START reAuthenticateUser]
/**
 * TODO.. ReAuthentication of user
 *
 * @param oldPassword
 * @param userReAuthenticateCallback
 */
fun reAuthenticateUser(oldPassword: String, userReAuthenticateCallback: UserReAuthenticateCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    val currUser = getUser()!!
    val credentials = EmailAuthProvider.getCredential(currUser.email!!, oldPassword)
    currUser.reauthenticate(credentials).addOnCompleteListener { task ->
        when {
            task.isSuccessful -> {
                Log.i(TAG, Throwable().stackTrace[0].methodName + " : success")
                userReAuthenticateCallback.onUserReAuthenticateSuccess()
            }
            else -> {
                val error = task.exception!!.localizedMessage.toString()
                Log.e(TAG, error)
                userReAuthenticateCallback.onUserReAuthenticateFailure(error)

            }
        }
    }
}
// [END reAuthenticateUser]


// [START updatePassword]
/**
 * TODO.. Update password of user
 *
 * @param newPassword
 * @param updatePasswordCallback
 */
fun updatePassword(newPassword: String, updatePasswordCallback: UpdatePasswordCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    getUser()!!.updatePassword(newPassword).addOnCompleteListener { task ->
        when {
            task.isSuccessful -> {
                Log.i(TAG, Throwable().stackTrace[0].methodName + " : success")
                updatePasswordCallback.onUpdatePasswordSuccess()
            }
            else -> {
                val error = task.exception!!.localizedMessage.toString()
                Log.e(TAG, error)
                updatePasswordCallback.onUpdatePasswordFailure(error)
            }
        }
    }
}
// [END updatePassword]

// [START authenticateUser]
/**
 * TODO Authentication of user if user login with social accounts
 *
 * @param isFacebook
 * @param authUserCallback
 */
fun Activity.authenticateUser(isFacebook: Boolean, authUserCallback: AuthUserCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    if (isFacebook) {
        Log.i(TAG, "getFBCallback")
        getFBCallback(object : SocialLoginCallback {
            override fun onSocialLoginSuccess(userProfile: UserProfile?) {
                Log.i(TAG, Throwable().stackTrace[0].methodName + " : success")
                firebaseAuthWithFacebook(userProfile!!, authUserCallback)
            }

            override fun onSocialLoginFailure(errorMessage: String?) {
                Log.e(TAG, errorMessage!!.toString())
                authUserCallback.onAuthUserFailure(errorMessage.toString())
            }
        })
    } else {
        firebaseAuthWithGoogle(getGProfile()!!, authUserCallback)
    }
}
// [END authenticateUser]


// [START firebaseAuthWithGoogle]
/**
 * TODO Authenticate google user
 *
 * @param userProfile
 * @param authUserCallback
 */
fun Activity.firebaseAuthWithGoogle(userProfile: UserProfile, authUserCallback: AuthUserCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    val credential = GoogleAuthProvider.getCredential(userProfile.idToken, null)
    getAuth().signInWithCredential(credential)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success
                Log.i(TAG, Throwable().stackTrace[0].methodName + " : success")
                authUserCallback.onAuthUserSuccess(userProfile)
            } else {
                // Sign in failed
                val error = task.exception!!.localizedMessage.toString()
                Log.e(TAG, error)
                authUserCallback.onAuthUserFailure(error)
            }
        }
}
// [END firebaseAuthWithGoogle]


// [START firebaseAuthWithFacebook]
/**
 * TODO Authenticate Facebook user
 *
 * @param userProfile
 * @param authUserCallback
 */
fun Activity.firebaseAuthWithFacebook(userProfile: UserProfile, authUserCallback: AuthUserCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    val accessToken = AccessToken.getCurrentAccessToken()
    val isLoggedIn = accessToken != null && !accessToken.isExpired
    if (isLoggedIn) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        getAuth().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success,
                    Log.i(TAG, Throwable().stackTrace[0].methodName + " : success")
                    authUserCallback.onAuthUserSuccess(userProfile)
                } else {
                    // Sign in failed
                    val error = task.exception!!.localizedMessage.toString()
                    Log.e(TAG, error)
                    authUserCallback.onAuthUserFailure(error)

                }
            }
    } else {
        authUserCallback.onAuthUserFailure(getString(R.string.session_expired))
    }
}
// [END firebaseAuthWithFacebook]


// [START deleteAccount]
/**
 * TODO Delete user account
 *
 * @param deleteCallback
 */
fun deleteAccount(deleteCallback: DeleteCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    getUser()!!.delete().addOnCompleteListener { delete ->
        when {
            delete.isSuccessful -> {
                Log.i(TAG, Throwable().stackTrace[0].methodName + " : success")
                deleteCallback.onDeleteDocumentSuccess()
            }
            else -> {
                val error = delete.exception!!.localizedMessage.toString()
                Log.e(TAG, error)
                deleteCallback.onDeleteDocumentFailure(error)
            }
        }
    }
}
// [END deleteAccount]