package com.firebase.demo.sociallogin

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.firebase.demo.sociallogin.callback.SignInCallback
import com.firebase.demo.sociallogin.callback.SignOutCallback
import com.firebase.demo.sociallogin.dao.UserProfile
import org.json.JSONException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


// https://developers.facebook.com/docs/facebook-login/android/

private const val TAG = "FBLogin"

fun getFBAccessToken(): AccessToken? {
    return AccessToken.getCurrentAccessToken()
}

fun isFBSignIn(): Boolean {
    // Check for existing Facebook Sign In account, if the user is already signed in the FacebookSignInAccount will be non-null.
    return getFBAccessToken() != null
}

fun LoginButton.setFBPermissions() {
    setPermissions(listOf("email", "public_profile"))
}

fun getCallBackManager(): CallbackManager? {
    return CallbackManager.Factory.create()
}

fun LoginButton.setFBCallback(callbackManager: CallbackManager?, fbSignInCallback: SignInCallback) {
    registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
        override fun onSuccess(loginResult: LoginResult?) {
            val loggedIn = AccessToken.getCurrentAccessToken() == null
            Log.d(TAG, "$loggedIn ??")
            val request = GraphRequest.newMeRequest(getFBAccessToken()) { `object`, _ ->
                Log.d(TAG, `object`.toString())
                try {
                    val firstName = `object`.getString("first_name")
                    val lastName = `object`.getString("last_name")
                    val emailAddress = `object`.getString("email")
                    val id = `object`.getString("id")
                    val photoUrl = "https://graph.facebook.com/$id/picture?type=normal"
                    val fullName = "$firstName $lastName"
                    val user = UserProfile(fullName, emailAddress, Uri.parse(photoUrl), id, getFBAccessToken()!!.token, true)
                    fbSignInCallback.onLoginSuccess(user)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, "signInResult: $e")
                    fbSignInCallback.onLoginFailure(e.toString())
                }
            }
            val parameters = Bundle()
            parameters.putString("fields", "first_name,last_name,email,id")
            request.parameters = parameters
            request.executeAsync()
        }

        override fun onCancel() {
            fbSignInCallback.onLoginFailure("Sign in with facebook is cancel")
        }

        override fun onError(exception: FacebookException) {
            Log.e(TAG, exception.toString())
            fbSignInCallback.onLoginFailure(exception.localizedMessage)
        }
    })
}


fun getFBCallback(fbSignInCallback: SignInCallback) {
    val request = GraphRequest.newMeRequest(getFBAccessToken()) { `object`, _ ->
        Log.d(TAG, `object`.toString())
        try {
            val firstName = `object`.getString("first_name")
            val lastName = `object`.getString("last_name")
            val emailAddress = `object`.getString("email")
            val id = `object`.getString("id")
            val photoUrl = "https://graph.facebook.com/$id/picture?type=normal"
            val fullName = "$firstName $lastName"
            val user = UserProfile(fullName, emailAddress, Uri.parse(photoUrl), id, getFBAccessToken()!!.token, true)
            fbSignInCallback.onLoginSuccess(user)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "signInResult: $e")
            fbSignInCallback.onLoginFailure(e.toString())
        }
    }
    val parameters = Bundle()
    parameters.putString("fields", "first_name,last_name,email,id")
    request.parameters = parameters
    request.executeAsync()
}

fun fbSignOut(signOutCallback: SignOutCallback) {
    if (getFBAccessToken() != null) {
        LoginManager.getInstance().logOut()
    }
    signOutCallback.onSignOut()
}

fun Context.printHashKey() {
    try {
        val info: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        for (signature in info.signatures) {
            val md: MessageDigest = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            val hashKey = String(Base64.encode(md.digest(), 0))
            Log.i(TAG, "Hash Key: $hashKey")
        }
    } catch (e: NoSuchAlgorithmException) {
        Log.e(TAG, "printHashKey", e)
    } catch (e: Exception) {
        Log.e(TAG, "printHashKey", e)
    }
}
