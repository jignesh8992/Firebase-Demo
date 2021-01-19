package com.firebase.demo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.jdrodi.BaseActivity
import com.firebase.demo.LoginActivity
import com.firebase.demo.R
import com.firebase.demo.sociallogin.callback.SignInCallback
import com.firebase.demo.sociallogin.callback.SignOutCallback
import com.firebase.demo.sociallogin.dao.UserProfile
import com.firebase.demo.sociallogin.fbSignOut
import com.firebase.demo.sociallogin.gSignOut
import com.firebase.demo.sociallogin.getFBCallback
import com.firebase.demo.sociallogin.getGProfile
import kotlinx.android.synthetic.main.activity_profile.*

private val TAG = ProfileActivity::class.qualifiedName

class ProfileActivity : BaseActivity(), SignOutCallback {

    var isFb = true

    companion object {
        fun newIntent(mContext: Context, isFb: Boolean): Intent {
            val intent = Intent(mContext, ProfileActivity::class.java)
            intent.putExtra("is_fb", isFb)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }

    override fun getContext(): Activity {
        return this@ProfileActivity
    }

    override fun initActions() {
        logoutBtn.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        super.onClick(view)
        when (view) {
            logoutBtn -> {
                if (isFb) {
                    fbSignOut(this)
                } else {
                    gSignOut(this)
                }
            }
        }
    }

    override fun initAds() {

    }

    override fun initData() {
        isFb = intent.getBooleanExtra("is_fb", false)

        if (isFb) {
            getFBCallback(object : SignInCallback {
                override fun onLoginSuccess(isFb: Boolean, userProfile: UserProfile?) {
                    setupUserProfile(userProfile)
                }

                override fun onLoginFailure(errorMessage: String?) {

                }
            })
        } else {
            setupUserProfile(getGProfile())
        }
    }

    private fun setupUserProfile(gProfile: UserProfile?) {
        if (gProfile != null) {
            name!!.text = gProfile.displayName
            email!!.text = gProfile.email
            userId!!.text = gProfile.accountid
            try {
                Glide.with(this).load(gProfile.photoUrl).into(profileImage!!)
            } catch (e: NullPointerException) {
                Toast.makeText(applicationContext, "image not found", Toast.LENGTH_LONG).show()
            }
        } else {
            gotoMainActivity()
        }
    }

    private fun gotoMainActivity() {
        startActivity(LoginActivity.newIntent(mContext))
    }

    override fun onSignOut() {
        Toast.makeText(applicationContext, "Sign out successfully", Toast.LENGTH_LONG).show()
        gotoMainActivity()
    }
}