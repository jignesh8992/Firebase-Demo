package com.firebase.demo.sociallogin

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.firebase.demo.R
import com.firebase.demo.callback.SocialLoginCallback
import com.firebase.demo.callback.SignOutCallback
import com.firebase.demo.sociallogin.dao.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener

// https://developers.google.com/identity/sign-in/android/start-integrating
// https://developers.google.com/identity/sign-in/android/backend-auth


// Request code
const val RC_SIGN_IN = 1

private const val TAG = "GLogin"


fun Activity.isGSignIn(): Boolean {
    // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
    return GoogleSignIn.getLastSignedInAccount(this) != null
}

fun Activity.getGoogleClient(): GoogleSignInClient? {
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    val gso = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.google_client_id))
        .requestEmail()
        .build()

    // Build a GoogleSignInClient with the options specified by gso.
    return GoogleSignIn.getClient(this, gso)
}

fun Activity.gSignIn() {
    val signInIntent = getGoogleClient()?.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
}

fun gSignInResult(data: Intent, gSocialLoginCallback: SocialLoginCallback) {
    Log.i(TAG, "gSignInResult")
    try {
        // The Task returned from this call is always completed, no need to attach
        // a listener.
        val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account: GoogleSignInAccount? = completedTask!!.getResult(ApiException::class.java)
        // Signed in successfully, show authenticated UI.
        if (account != null) {
            // Google Sign In was successful, authenticate with Firebase
            Log.i(TAG, "isSuccess: " + account.idToken)
            val profile = UserProfile(account.displayName, account.email, account.photoUrl, account.id, account.idToken, false)
            gSocialLoginCallback.onSocialLoginSuccess(profile)
        } else {
            Log.e(TAG, "Sign in with google cancel")
            gSocialLoginCallback.onSocialLoginFailure("Sign in with google cancel")
        }


    } catch (e: ApiException) {
        // The ApiException status code indicates the detailed failure reason.
        // Please refer to the GoogleSignInStatusCodes class reference for more information.
        Log.e(TAG, "signInResult:failed code=" + e.statusCode + ": " + e.localizedMessage.toString())
    }
}


fun Activity.getGProfile(): UserProfile? {
    val account = GoogleSignIn.getLastSignedInAccount(this)
    if (account != null) {
        Log.i(TAG, "isSuccess")
        return UserProfile(account.displayName, account.email, account.photoUrl, account.id, account.idToken, false)
    }
    return null
}


fun Activity.gSignOut(signOutCallback: SignOutCallback?) {
    getGoogleClient()!!.signOut()
        .addOnCompleteListener(this) {
            signOutCallback?.onSignOut()
        }
}

fun Activity.gRevokeAccess(signOutCallback: SignOutCallback) {
    getGoogleClient()!!.revokeAccess()
        .addOnCompleteListener(this, OnCompleteListener<Void?> {
            signOutCallback.onSignOut()
        })
}