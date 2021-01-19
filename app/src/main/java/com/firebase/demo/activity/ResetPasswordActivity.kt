package com.firebase.demo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.toast
import com.firebase.demo.R
import com.firebase.demo.utilities.isValidPassword
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_reset_password.*

private val TAG = ResetPasswordActivity::class.qualifiedName

class ResetPasswordActivity : BaseActivity() {

    private var fAuth: FirebaseAuth? = null

    companion object {
        fun newIntent(mContext: Context): Intent {
            return Intent(mContext, ResetPasswordActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
    }

    override fun getContext(): Activity {
        return this@ResetPasswordActivity
    }

    override fun initActions() {

    }

    override fun initAds() {

    }

    override fun initData() {
        fAuth = FirebaseAuth.getInstance()


        fAuth!!.sendPasswordResetEmail(fAuth!!.currentUser!!.email!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    toast("A link to change the password has been sent to the registered email id")
                }
            }
    }

    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {
            iv_back -> {
                onBackPressed()
            }
            btn_reset -> {
                resetPassword()
            }
        }
    }

    private fun resetPassword() {
        val oldPassword = et_old_password.editText!!.text.toString().trim()
        val newPassword = et_new_password.editText!!.text.toString().trim()
        if (et_old_password.isValidPassword() && et_new_password.isValidPassword()) {

            hideKeyboard()

            jpShow()

            val currUser = fAuth!!.currentUser
            val credentials = EmailAuthProvider.getCredential(currUser!!.email!!, oldPassword)
            currUser.reauthenticate(credentials).addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        currUser.updatePassword(newPassword).addOnCompleteListener { task ->
                            jpDismiss()
                            when {
                                task.isSuccessful -> {
                                    toast("Password changes successfully")
                                }
                                else -> {
                                    toast("Password change failed: ${task.exception!!.localizedMessage}")
                                }
                            }
                        }
                    }
                    else -> {
                        jpDismiss()
                        toast("Password change failed: ${task.exception!!.localizedMessage}")
                    }
                }
            }
        } else {
            et_old_password.isValidPassword()
            et_new_password.isValidPassword()
        }
    }
}


