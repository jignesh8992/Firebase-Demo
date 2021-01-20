package com.firebase.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.firebase.demo.activity.DashboardActivity
import com.firebase.demo.activity.RegistrationActivity
import com.firebase.demo.sociallogin.*
import com.firebase.demo.sociallogin.callback.SignInCallback
import com.firebase.demo.sociallogin.dao.UserProfile
import com.firebase.demo.utilities.*
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_social_login.*


private val TAG = LoginActivity::class.qualifiedName

class LoginActivity : BaseActivity(), SignInCallback {


    var callbackManager: CallbackManager? = null
    var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null

    companion object {
        fun newIntent(mContext: Context): Intent {
            return Intent(mContext, LoginActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }


    override fun getContext(): Activity {
        return this@LoginActivity
    }

    override fun initActions() {
        iv_google.setOnClickListener(this)
        iv_fb.setOnClickListener(this)
        btn_login.setOnClickListener(this)
        tv_signup.setOnClickListener(this)

    }

    override fun initAds() {

    }

    override fun initData() {

        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        /* if (isGSignIn()) {
             firebaseAuthWithGoogle(getGProfile())
         }

         if (isFBSignIn()) {
             getFBCallback(object : SignInCallback {
                 override fun onLoginSuccess(userProfile: UserProfile?) {

                 }

                 override fun onLoginFailure(errorMessage: String?) {

                 }
             })
         }*/

        //  login_btb_fb.loginBehavior= LoginBehavior.DEVICE_AUTH
        callbackManager = getCallBackManager()
        login_btb_fb.setFBPermissions()
        login_btb_fb.setFBCallback(callbackManager, this)


    }

    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {
            iv_google -> {
                gSignIn()
            }
            iv_fb -> {
                login_btb_fb.performClick()
            }
            tv_signup -> {
                startActivity(RegistrationActivity.newIntent(mContext))
            }

            btn_login -> {
                loginUser()
            }
        }

    }

    private fun loginUser() {
        val email = et_email.editText!!.text.toString().trim()
        val password = et_password.editText!!.text.toString().trim()

        if (et_email.isValidEmail() && et_password.isValidPassword()) {
            hideKeyboard()
            jpShow()
            fAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                jpDismiss()

                when {
                    task.isSuccessful -> {
                        val currUser = fAuth!!.currentUser
                        if (currUser != null && currUser.isEmailVerified) {
                            toast(getString(R.string.login_successfully))
                            startActivity(DashboardActivity.newIntent(mContext))
                            finish()
                        } else if (currUser != null && !currUser.isEmailVerified) {
                            currUser.sendEmailVerification().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    toast("Verification email sent to ")
                                } else {
                                    Log.e(TAG, "sendEmailVerification", task.exception)
                                    toast("Failed to send verification email.")
                                }
                            }
                        }

                    }
                    task.isCanceled -> {
                        toast(getString(R.string.login_canceled))
                    }
                    else -> {
                        toast(getString(R.string.login_failed) + task.exception.toString())
                        Log.e(TAG, task.exception.toString())
                    }
                }
            }
        } else {
            et_email.isValidEmail()
            et_password.isValidPassword()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            gSignInResult(data!!, this)
        }
    }


    override fun onLoginSuccess(userProfile: UserProfile?) {
        if (userProfile!!.isFb) {
            handleFacebookAccessToken(userProfile)
        } else {
            firebaseAuthWithGoogle(userProfile)
        }
    }

    override fun onLoginFailure(errorMessage: String?) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun goProfile() {
        startActivity(DashboardActivity.newIntent(mContext))
        finish()
    }


    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(userProfile: UserProfile?) {
        // [START_EXCLUDE silent]
        jpShow()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(userProfile!!.idToken, null)
        fAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")


                    val userId = fAuth!!.currentUser!!.uid
                    val docRef = fStore!!.collection(COLLECTION_USER).document(userId)

                    docRef.get().addOnSuccessListener {
                        if (it.exists()) {
                            toast(getString(R.string.login_successfully))
                            startActivity(DashboardActivity.newIntent(mContext))
                            finish()
                        } else {
                            val user: HashMap<String, Any> = HashMap()
                            user[KEY_NAME] = userProfile.displayName!!
                            user[KEY_EMAIL] = userProfile.email!!
                            user[KEY_PROFILE] = userProfile.photoUrl!!.toString()

                            Log.d(TAG, "success: photoUrl->" + userProfile.photoUrl!!)

                            docRef.set(user).addOnCompleteListener { result ->
                                when {
                                    result.isSuccessful -> {
                                        toast(getString(R.string.login_successfully))
                                        startActivity(DashboardActivity.newIntent(mContext))
                                        finish()
                                    }
                                    else -> {
                                        toast(getString(R.string.registration_failed) + result.exception.toString())
                                        Log.e(TAG, task.exception.toString())
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    toast("Authentication Failed: ${task.exception}")
                }

                // [START_EXCLUDE]
                jpDismiss()
                // [END_EXCLUDE]
            }
    }
    // [END auth_with_google]


    // [START auth_with_facebook]
    private fun handleFacebookAccessToken(userProfile: UserProfile?) {
        Log.d(TAG, "signInWithCredential:${userProfile!!.idToken}")
        // [START_EXCLUDE silent]
        jpShow()
        // [END_EXCLUDE]


        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired

        if (isLoggedIn) {

            val credential = FacebookAuthProvider.getCredential(accessToken.token)
            fAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        toast(getString(R.string.login_successfully))

                        val userId = fAuth!!.currentUser!!.uid
                        val docRef = fStore!!.collection(COLLECTION_USER).document(userId)

                        docRef.get().addOnSuccessListener {
                            if (it.exists()) {
                                toast(getString(R.string.login_successfully))
                                startActivity(DashboardActivity.newIntent(mContext))
                                finish()
                            } else {
                                val user: HashMap<String, Any> = HashMap()
                                user[KEY_NAME] = userProfile.displayName!!
                                user[KEY_EMAIL] = userProfile.email!!
                                user[KEY_PROFILE] = userProfile.photoUrl!!.toString()

                                Log.d(TAG, "success: photoUrl->" + userProfile.photoUrl!!)

                                docRef.set(user).addOnCompleteListener { result ->
                                    when {
                                        result.isSuccessful -> {
                                            toast(getString(R.string.login_successfully))
                                            startActivity(DashboardActivity.newIntent(mContext))
                                            finish()
                                        }
                                        else -> {
                                            toast(getString(R.string.registration_failed) + result.exception.toString())
                                            Log.e(TAG, task.exception.toString())
                                        }
                                    }
                                }
                            }
                        }


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        toast("signInWithCredential:failure :" + task.exception)

                    }

                    // [START_EXCLUDE]
                    jpDismiss()
                    // [END_EXCLUDE]
                }
        } else {
            toast("Expired:")
        }
    }
    // [END auth_with_facebook]

}
