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
import com.firebase.demo.callback.PasswordResetCallback
import com.firebase.demo.callback.UpdatePasswordCallback
import com.firebase.demo.callback.UserReAuthenticateCallback
import com.firebase.demo.utilities.isValidPassword
import com.firebase.demo.utilities.reAuthenticateUser
import com.firebase.demo.utilities.sendPasswordResetEmail
import com.firebase.demo.utilities.updatePassword
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_reset_password.*

private val TAG = ResetPasswordActivity::class.qualifiedName

class ResetPasswordActivity : BaseActivity(), UserReAuthenticateCallback, UpdatePasswordCallback {

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

        sendPasswordResetEmail(object : PasswordResetCallback {
            override fun onPasswordResetSuccess() {
                Log.d(TAG, "Email sent.")
                toast("A link to change the password has been sent to the registered email id")
            }

            override fun onPasswordResetFailure(errorMessage: String) {

            }
        })
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
        if (et_old_password.isValidPassword() && et_new_password.isValidPassword()) {
            hideKeyboard()
            jpShow()
            reAuthenticateUser(oldPassword, this)
        } else {
            et_old_password.isValidPassword()
            et_new_password.isValidPassword()
        }
    }

    // [START UserReAuthenticate_callback]
    override fun onUserReAuthenticateSuccess() {
        val newPassword = et_new_password.editText!!.text.toString().trim()
        updatePassword(newPassword, this)
    }

    override fun onUserReAuthenticateFailure(errorMessage: String) {
        jpDismiss()
        toast(errorMessage)
    }
    // [END UserReAuthenticate_callback]


    // [START UpdatePassword_callback]
    override fun onUpdatePasswordSuccess() {
        toast("Password changes successfully")
        jpDismiss()
    }

    override fun onUpdatePasswordFailure(errorMessage: String) {
        jpDismiss()
        toast(errorMessage)
    }
    // [END UpdatePassword_callback]
}


