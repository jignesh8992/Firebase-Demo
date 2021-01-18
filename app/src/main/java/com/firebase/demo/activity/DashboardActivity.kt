package com.firebase.demo.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.ChooserHelper.chooseImage
import com.example.jdrodi.utilities.FileHelper.getPath
import com.example.jdrodi.utilities.REQUEST_PHOTO
import com.example.jdrodi.utilities.loadImage
import com.example.jdrodi.utilities.showPermissionsAlert
import com.example.jdrodi.utilities.toast
import com.firebase.demo.LoginActivity
import com.firebase.demo.R
import com.firebase.demo.utilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.et_email
import kotlinx.android.synthetic.main.activity_dashboard.et_full_name
import kotlinx.android.synthetic.main.activity_registration.*

private val TAG = DashboardActivity::class.qualifiedName

private var storagePermission = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class DashboardActivity : BaseActivity() {

    private var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
    }

    override fun getContext(): Activity {
        return this@DashboardActivity
    }

    override fun initActions() {

    }

    override fun initAds() {

    }

    override fun initData() {

        et_email.disableEditText()
        et_full_name.disableEditText()

        fStore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()
        if (fAuth!!.currentUser != null) {
            et_email.editText!!.setText(fAuth!!.currentUser!!.email)
        }

        val userId = fAuth!!.currentUser!!.uid
        val docRef = fStore!!.collection(COLLECTION_USER).document(userId)
        docRef.addSnapshotListener(mContext) { value, error ->
            if (value != null) {
                et_full_name.editText!!.setText(value.getString(KEY_NAME))

                val path = value.getString(KEY_PROFILE)
                iv_profile.tag = path
                loadImage(path!!, iv_profile, R.mipmap.ic_launcher_round)

            } else {
                toast(error!!.localizedMessage)
            }
        }
    }

    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {

            btn_update -> {
                updateUser()
            }

            iv_edit_profile -> {
                checkStoragePermission()
            }

            btn_logout -> {
                fAuth!!.signOut()
                toast(getString(R.string.logout_successfully))
                startActivity(Intent(mContext, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun updateUser() {

        val name = et_full_name.editText!!.text.toString().trim()
        val email = et_email.editText!!.text.toString().trim()
        val userId = fAuth!!.currentUser!!.uid
        val docRef = fStore!!.collection(COLLECTION_USER).document(userId)
        val user: HashMap<String, Any> = HashMap()
        user[KEY_NAME] = name
        user[KEY_EMAIL] = email
        user[KEY_PROFILE] = iv_profile.tag
        docRef.update(user).addOnCompleteListener { result ->
            when {
                result.isSuccessful -> {
                    toast(getString(R.string.update_successfully))
                }
                else -> {
                    toast(getString(R.string.update_failed) + result.exception.toString())
                }
            }
        }
    }

    private fun checkStoragePermission() {
        Dexter.withContext(mContext)
            .withPermissions(*storagePermission)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }

                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    when {
                        report.areAllPermissionsGranted() -> {
                            chooseImage(REQUEST_PHOTO)
                        }
                        report.isAnyPermissionPermanentlyDenied -> showPermissionsAlert("")
                        else -> toast("All permissions are not granted..")
                    }
                }
            }).withErrorListener {
                toast("Error occurred! ")
            }.onSameThread()
            .check()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PHOTO) {
            when (resultCode) {
                RESULT_OK -> {
                    val path = getPath(data!!.data!!)
                    iv_profile.tag = path
                    loadImage(path!!, iv_profile, R.mipmap.ic_launcher_round)
                }
                RESULT_CANCELED -> {

                }
                else -> {

                }
            }
        }
    }


}