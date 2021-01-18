package com.firebase.demo.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.toast
import com.facebook.CallbackManager
import com.firebase.demo.LoginActivity
import com.firebase.demo.R
import com.firebase.demo.sociallogin.*
import com.firebase.demo.sociallogin.callback.SignInCallback
import com.firebase.demo.sociallogin.dao.UserProfile
import com.firebase.demo.utilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.layout_social_login.*
import java.util.*
import kotlin.collections.HashMap

private val TAG = RegistrationActivity::class.qualifiedName

class RegistrationActivity : BaseActivity(), SignInCallback {


    private var callbackManager: CallbackManager? = null
    private var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }

    override fun getContext(): Activity {
        return this@RegistrationActivity
    }

    override fun initActions() {
        login_btb_google.setOnClickListener(this)
        iv_google.setOnClickListener(this)
        login_btb_fb.setOnClickListener(this)
        iv_fb.setOnClickListener(this)
        btn_register.setOnClickListener(this)
        tv_login.setOnClickListener(this)
    }

    override fun initAds() {

    }

    override fun initData() {

        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        if (fAuth!!.currentUser != null) {
            toast(getString(R.string.already_registered))
            startActivity(Intent(mContext, DashboardActivity::class.java))
            finish()
        }


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
            tv_login -> {
                startActivity(Intent(mContext, LoginActivity::class.java))
            }
            btn_register -> {
                registerUser()
            }
        }

    }

    private fun registerUser() {
        val name = et_full_name.editText!!.text.toString().trim()
        val email = et_email.editText!!.text.toString().trim()
        val password = et_password.editText!!.text.toString().trim()

        if (et_full_name.isValidName() && et_email.isValidEmail() && et_password.isValidPassword() && et_repeat_password.isValidRepeatPassword(et_password)) {

            fAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        val userId = fAuth!!.currentUser!!.uid
                        val docRef = fStore!!.collection(COLLECTION_USER).document(userId)
                        val user: HashMap<String, Any> = HashMap()
                        user[KEY_NAME] = name
                        user[KEY_EMAIL] = email
                        user[KEY_PROFILE] = ""
                        docRef.set(user).addOnCompleteListener { result ->
                            when {
                                result.isSuccessful -> {
                                    toast(getString(R.string.registration_successfully))
                                    startActivity(Intent(mContext, DashboardActivity::class.java))
                                    finish()
                                }
                                else -> {
                                    toast(getString(R.string.registration_failed) + result.exception.toString())
                                    Log.e(TAG, task.exception.toString())
                                }
                            }
                        }
                    }
                    task.isCanceled -> {
                        toast(getString(R.string.registration_canceled))
                    }
                    else -> {
                        toast(getString(R.string.registration_failed) + task.exception.toString())
                        Log.e(TAG, task.exception.toString())
                    }
                }
            }


        } else {
            et_full_name.isValidName()
            et_email.isValidEmail()
            et_password.isValidPassword()
            et_repeat_password.isValidRepeatPassword(et_password)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
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


