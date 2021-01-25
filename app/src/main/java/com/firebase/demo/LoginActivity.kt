package com.firebase.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.toast
import com.facebook.CallbackManager
import com.firebase.demo.activity.DashboardActivity
import com.firebase.demo.activity.RegistrationActivity
import com.firebase.demo.callback.*
import com.firebase.demo.sociallogin.*
import com.firebase.demo.sociallogin.dao.UserProfile
import com.firebase.demo.utilities.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_social_login.*


private val TAG = LoginActivity::class.qualifiedName

class LoginActivity : BaseActivity(), LoginCallback, SocialLoginCallback, AuthUserCallback, AddUserCallback {


    var callbackManager: CallbackManager? = null

    companion object {
        fun newIntent(mContext: Context): Intent {
            val intent = Intent(mContext, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
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
        tv_forget_password.setOnClickListener(this)

    }

    override fun initAds() {

    }

    override fun initData() {

        printHashKey()

        //  login_btb_fb.loginBehavior= LoginBehavior.DEVICE_AUTH
        callbackManager = getCallBackManager()
        login_btb_fb.setFBPermissions()
        login_btb_fb.setFBCallback(callbackManager, this)


    }

    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {
            iv_google -> {
                if (isGSignIn()) {
                    authenticateUser(false, this)

                } else {
                    gSignIn()
                }
            }
            iv_fb -> {
                if (isFBSignIn()) {
                    authenticateUser(true, this)
                } else {
                    login_btb_fb.performClick()
                }
            }

            tv_forget_password -> {
                jpShow()
                sendPasswordResetEmail(object : PasswordResetCallback {
                    override fun onPasswordResetSuccess() {
                        jpDismiss()
                        Log.d(TAG, "Email sent.")
                        toast("A link to change the password has been sent to the registered email id")
                    }

                    override fun onPasswordResetFailure(errorMessage: String) {
                        jpDismiss()
                        toast(errorMessage)
                    }
                })
            }

            tv_signup -> {
                startActivity(RegistrationActivity.newIntent(mContext))
            }

            btn_login -> {
                verifyUser()
            }
        }

    }


    private fun verifyUser() {
        val email = et_email.editText!!.text.toString().trim()
        val password = et_password.editText!!.text.toString().trim()

        if (et_email.isValidEmail() && et_password.isValidPassword()) {
            hideKeyboard()
            jpShow()
            signInUser(email, password, this)
        } else {
            et_email.isValidEmail()
            et_password.isValidPassword()
        }
    }

    private fun redirectUserToDashboard() {
        toast(getString(R.string.login_successfully))
        startActivity(DashboardActivity.newIntent(mContext))
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            jpShow()
            gSignInResult(data!!, this)
        }
    }

    // [START LoginCallback_callback]
    override fun onLoginSuccess() {
        Log.i(TAG, "onLoginSuccess")
        jpDismiss()
        redirectUserToDashboard()
    }

    override fun onLoginVerification() {
        Log.i(TAG, "onLoginVerification")
        jpDismiss()
        toast(getString(R.string.email_sent))
    }

    override fun onLoginFailure(errorMessage: String) {
        Log.e(TAG, "onLoginFailure: $errorMessage")
        jpDismiss()
        toast(errorMessage)
    }
    // [END LoginCallback_callback]


    // [START SocialLoginCallback_callback]
    override fun onSocialLoginSuccess(userProfile: UserProfile?) {
        val loginType = if (userProfile!!.isFb) {
            "Facebook"
        } else {
            "Google"
        }
        Log.i(TAG, "onSocialLoginSuccess: $loginType")
        jpShow()
        authenticateUser(userProfile.isFb, this)
    }

    override fun onSocialLoginFailure(errorMessage: String?) {
        Log.e(TAG, "onSocialLoginFailure: $errorMessage")
        toast(errorMessage!!)
        jpDismiss()
    }
    // [END SocialLoginCallback_callback]


    // [START auth_callback]
    override fun onAuthUserSuccess(userProfile: UserProfile) {
        Log.i(TAG, "onAuthUserSuccess")
        checkAndInsertDocument(userProfile, this)
    }

    override fun onAuthUserFailure(errorMessage: String) {
        Log.e(TAG, "onAuthUserFailure: $errorMessage")
        jpDismiss()
        toast(errorMessage)
    }
    // [END auth_callback]


    // [START add_user_callback]
    override fun onUserAddedSuccess(userProfile: UserProfile) {
        Log.i(TAG, "onUserAddedSuccess")
        redirectUserToDashboard()
        jpDismiss()
    }

    override fun onUserExist(userProfile: UserProfile) {
        Log.i(TAG, "onUserExist")
        redirectUserToDashboard()
        jpDismiss()
    }

    override fun onUserAddFailure(errorMessage: String) {
        Log.e(TAG, "onUserAddFailure: $errorMessage")
        jpDismiss()
        toast(errorMessage)
    }
    // [START add_user_callback]

}
