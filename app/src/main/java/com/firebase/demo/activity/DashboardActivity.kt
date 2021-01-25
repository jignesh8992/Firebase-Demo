package com.firebase.demo.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.jdrodi.BaseActivity
import com.example.jdrodi.utilities.*
import com.example.jdrodi.utilities.ChooserHelper.chooseImage
import com.example.jdrodi.utilities.FileHelper.getPath
import com.firebase.demo.LoginActivity
import com.firebase.demo.R
import com.firebase.demo.callback.GetDocumentCallback
import com.firebase.demo.callback.PasswordResetCallback
import com.firebase.demo.callback.StorageUploadCallback
import com.firebase.demo.callback.UpdateDocumentCallback
import com.firebase.demo.sociallogin.dao.User
import com.firebase.demo.sociallogin.fbSignOut
import com.firebase.demo.sociallogin.gSignOut
import com.firebase.demo.sociallogin.isFBSignIn
import com.firebase.demo.sociallogin.isGSignIn
import com.firebase.demo.utilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.et_email
import kotlinx.android.synthetic.main.activity_dashboard.et_full_name
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*


private val TAG = DashboardActivity::class.qualifiedName

private var storagePermission = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class DashboardActivity : BaseActivity(), GetDocumentCallback, PasswordResetCallback {

    private var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null
    private var mStorageRef: StorageReference? = null

    companion object {
        fun newIntent(mContext: Context): Intent {
            val intent = Intent(mContext, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

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
        mStorageRef = FirebaseStorage.getInstance().reference


        val fProvider = getProvider()
        if (fProvider != PROVIDER_PASSWORD) {
            btn_delete.visibility = View.GONE
        }


        jpShow()
        getDocument(this)

    }


    override fun onClick(view: View) {
        super.onClick(view)

        when (view) {

            btn_update -> {
                updateUser()
            }

            btn_reset_password -> {
                val fProvider = getProvider()
                if (fProvider != PROVIDER_PASSWORD) {
                    jpShow()
                    sendPasswordResetEmail(this)
                } else {
                    startActivity(ResetPasswordActivity.newIntent(mContext))
                }
            }

            iv_edit_profile -> {
                checkStoragePermission()
            }
            btn_delete -> {
                startActivity(DeleteAccountActivity.newIntent(mContext))
            }

            btn_logout -> {
                fAuth!!.signOut()
                if (isFBSignIn()) {
                    fbSignOut(null)
                }
                if (isGSignIn()) {
                    gSignOut(null)
                }
                toast(getString(R.string.logout_successfully))
                startActivity(LoginActivity.newIntent(mContext))
                finish()
            }
        }
    }

    private fun updateUser() {
        hideKeyboard()
        jpShow()
        val name = et_full_name.editText!!.text.toString().trim()
        val email = et_email.editText!!.text.toString().trim()
        val profile = iv_profile.tag.toString()
        val user = User(name, email, profile)
        updateDocument(user, object : UpdateDocumentCallback {
            override fun onUpdateDocumentSuccess() {
                jpDismiss()
                toast(getString(R.string.update_successfully))
            }

            override fun onUpdateDocumentFailure(errorMessage: String) {
                jpDismiss()
                toast(errorMessage)
            }
        })
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
                    uploadFileOnFirebase(data!!.data!!)
                }
                RESULT_CANCELED -> {
                    toast("Image choose cancelled by user")
                }
                else -> {
                    toast("Something went wrong")
                }
            }
        }
    }

    private fun uploadFileOnFirebase(uri: Uri) {
        val pDialog = ProgressDialog(mContext)
        pDialog.setTitle("Uploading profile")
        pDialog.setCancelable(false)
        pDialog.show()
        val path = getPath(uri)
        uploadFile(path!!, object : StorageUploadCallback {
            override fun onUploadSuccess(downloadUrl: String) {
                jpDismiss()
                iv_profile.tag = downloadUrl
                Log.i(TAG, "Image URL : $downloadUrl")
                loadImage(downloadUrl, iv_profile, R.mipmap.ic_launcher_round)
                pDialog.dismiss()
            }

            override fun onUploadFailure(errorMessage: String) {
                jpDismiss()
                toast(errorMessage)
            }

            override fun onUploadProgress(progress: Int) {
                pDialog.setMessage("Progress: ${progress.toInt()}%")
            }
        })
    }

    // [START GetDocument_callback]
    override fun onGetDocumentSuccess(use: User) {
        et_full_name.editText!!.setText(use.displayName)
        et_email.editText!!.setText(use.email)
        val path = use.photoUrl
        iv_profile.tag = path
        loadImage(path!!, iv_profile, R.mipmap.ic_launcher_round)

        jpDismiss()
    }

    override fun onGetDocumentFailure(errorMessage: String) {
        jpDismiss()
        toast(errorMessage)
    }
    // [END GetDocument_callback]

    // [START PasswordReset_callback]
    override fun onPasswordResetSuccess() {
        jpDismiss()
        toast("A link to change the password has been sent to the registered email id")
    }

    override fun onPasswordResetFailure(errorMessage: String) {
        jpDismiss()
        toast(errorMessage)
    }
    // [END PasswordReset_callback]

}