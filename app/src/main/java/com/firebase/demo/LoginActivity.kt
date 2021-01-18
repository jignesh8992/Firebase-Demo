package com.firebase.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.jprogress.JProgress
import com.example.jdrodi.utilities.toast
import com.facebook.CallbackManager
import com.firebase.demo.activity.DashboardActivity
import com.firebase.demo.activity.ProfileActivity
import com.firebase.demo.activity.RegistrationActivity
import com.firebase.demo.sociallogin.*
import com.firebase.demo.sociallogin.callback.SignInCallback
import com.firebase.demo.sociallogin.dao.UserProfile
import com.firebase.demo.utilities.isValidEmail
import com.firebase.demo.utilities.isValidPassword
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_social_login.*

private val TAG = LoginActivity::class.qualifiedName

class LoginActivity : BaseActivity(), SignInCallback {


    var callbackManager: CallbackManager? = null
    private var pDialog: JProgress? = null
    var fAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }


    override fun getContext(): Activity {
        return this@LoginActivity
    }

    override fun initActions() {
        login_btb_google.setOnClickListener(this)
        iv_google.setOnClickListener(this)
        login_btb_fb.setOnClickListener(this)
        iv_fb.setOnClickListener(this)
        btn_login.setOnClickListener(this)
        tv_signup.setOnClickListener(this)

    }

    override fun initAds() {

    }

    override fun initData() {

        fAuth = FirebaseAuth.getInstance()

        pDialog = JProgress.create(this, JProgress.Style.SPIN_INDETERMINATE)
        if (isGSignIn()) {
            goProfile(false)
        }

        if (isFBSignIn()) {
            goProfile(true)
        }
        login_btb_fb.setFBPermissions()
        // login_btb_fb.loginBehavior= LoginBehavior.DEVICE_AUTH
        callbackManager = getCallBackManager()
        login_btb_fb.setFBCallback(callbackManager, this)


    }

    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {
            login_btb_google, iv_google -> {
                gSignIn()
            }
            login_btb_fb, iv_fb -> {
                login_btb_fb.performClick()
            }
            tv_signup -> {
                startActivity(Intent(mContext, RegistrationActivity::class.java))
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

            fAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        toast(getString(R.string.login_successfully))
                        startActivity(Intent(mContext, DashboardActivity::class.java))
                        finish()
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
        callbackManager?.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            gSignInResult(data!!, this)
        }
    }


    override fun onLoginSuccess(isFb: Boolean, userProfile: UserProfile?) {
        goProfile(isFb)
    }

    override fun onLoginFailure(errorMessage: String?) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun goProfile(isFb: Boolean) {
        val intent = Intent(mContext, ProfileActivity::class.java)
        intent.putExtra("is_fb", isFb)
        startActivity(intent)
    }

}
