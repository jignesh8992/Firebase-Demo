package com.firebase.demo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.hideKeyboard
import com.example.jdrodi.utilities.toast
import com.firebase.demo.LoginActivity
import com.firebase.demo.R
import com.firebase.demo.callback.DeleteCallback
import com.firebase.demo.callback.UserReAuthenticateCallback
import com.firebase.demo.utilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_delete_account.*
import kotlinx.android.synthetic.main.activity_delete_account.iv_back
import kotlinx.android.synthetic.main.activity_reset_password.*

private val TAG = DeleteAccountActivity::class.qualifiedName

class DeleteAccountActivity : BaseActivity(), UserReAuthenticateCallback, DeleteCallback {

    private var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null

    companion object {
        fun newIntent(mContext: Context): Intent {
            return Intent(mContext, DeleteAccountActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)
    }

    override fun getContext(): Activity {
        return this@DeleteAccountActivity
    }

    override fun initActions() {

    }

    override fun initAds() {

    }

    override fun initData() {
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
    }

    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {
            iv_back -> {
                onBackPressed()
            }
            btn_delete_account -> {
                deleteAccount()
            }
        }
    }

    private fun deleteAccount() {
        val oldPassword = et_password.editText!!.text.toString().trim()
        if (et_password.isValidPassword()) {
            hideKeyboard()
            jpShow()
            reAuthenticateUser(oldPassword, this)
        }
    }

    // [START UserReAuthenticate_callback]
    override fun onUserReAuthenticateSuccess() {
        deleteDocument(this)
    }

    override fun onUserReAuthenticateFailure(errorMessage: String) {
        jpDismiss()
        toast(errorMessage)
    }
    // [END UserReAuthenticate_callback]

    // [START Delete_callback]
    override fun onDeleteDocumentSuccess() {
        jpDismiss()
        toast("Account deleted successfully")
        startActivity(LoginActivity.newIntent(mContext))
    }

    override fun onDeleteDocumentFailure(errorMessage: String) {
        jpDismiss()
        toast(errorMessage)
    }
    // [END Delete_callback]
}


