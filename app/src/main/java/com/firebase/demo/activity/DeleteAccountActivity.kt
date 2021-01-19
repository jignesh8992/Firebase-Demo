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
import com.firebase.demo.LoginActivity
import com.firebase.demo.R
import com.firebase.demo.utilities.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_delete_account.*

private val TAG = DeleteAccountActivity::class.qualifiedName

class DeleteAccountActivity : BaseActivity() {

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
        val password = et_password.editText!!.text.toString().trim()
        if (et_password.isValidPassword()) {

            hideKeyboard()

            jpShow()

            val currUser = fAuth!!.currentUser
            val userId = fAuth!!.currentUser!!.uid
            val credentials = EmailAuthProvider.getCredential(currUser!!.email!!, password)
            currUser.reauthenticate(credentials).addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {

                        currUser.delete().addOnCompleteListener { delete ->
                            jpDismiss()
                            when {
                                delete.isSuccessful -> {
                                    val docRef = fStore!!.collection(COLLECTION_USER).document(userId)
                                    docRef.delete().addOnCompleteListener { result ->
                                        when {
                                            result.isSuccessful -> {
                                                toast("Account deleted successfully")
                                                startActivity(LoginActivity.newIntent(mContext))
                                            }
                                            else -> {
                                                toast("Delete account failed 1:" + result.exception.toString())
                                                Log.e(TAG, "Delete account failed 1:" + result.exception.toString())
                                            }
                                        }
                                    }

                                }
                                else -> {
                                    toast("Delete account failed 2: ${delete.exception!!.localizedMessage}")
                                    Log.e(TAG, "Delete account failed 2: ${delete.exception!!.localizedMessage}")
                                }
                            }
                        }
                    }
                    else -> {
                        jpDismiss()
                        toast("Delete account failed 3: ${task.exception!!.localizedMessage}")
                        Log.e(TAG, "Delete account failed 3: ${task.exception!!.localizedMessage}")
                    }
                }
            }
        }
    }
}


