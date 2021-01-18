package com.firebase.demo.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import java.io.File
import java.util.*
import kotlin.collections.HashMap


private val TAG = DashboardActivity::class.qualifiedName

private var storagePermission = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class DashboardActivity : BaseActivity() {

    private var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null
    private var mStorageRef: StorageReference? = null

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

                    val pDialog = ProgressDialog(mContext)
                    pDialog.setTitle("Uploading profile")
                    pDialog.setCancelable(false)
                    pDialog.show()
                    val userId = fAuth!!.currentUser!!.uid
                    val path = getPath(data!!.data!!)
                    val file: Uri = Uri.fromFile(File(path!!))
                    val imageRef: StorageReference = mStorageRef!!.child("images/$userId")


                    /*    val urlTask = imageRef.putFile(file)
                        urlTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                throw task.exception!!
                            }
                            // Continue with the task to get the download URL
                            imageRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            val downloadUrl = task.result
                            iv_profile.tag = downloadUrl
                            loadImage(downloadUrl!!, iv_profile, R.mipmap.ic_launcher_round)
                            pDialog.dismiss()
                        }*/

                    imageRef.putFile(file)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                val downloadUrl = uri.toString()
                                iv_profile.tag = downloadUrl
                                Log.i(TAG, "Image URL : $downloadUrl")
                                loadImage(downloadUrl, iv_profile, R.mipmap.ic_launcher_round)
                                pDialog.dismiss()
                            }
                        }
                        .addOnFailureListener { error ->
                            pDialog.dismiss()
                            toast("Error occurred! : ${error.localizedMessage}")
                            Log.e(TAG, "Error occurred! : ${error.localizedMessage}")
                        }.addOnProgressListener { taskSnapshot ->
                            val progress: Double = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                            pDialog.setMessage("Progress: ${progress.toInt()}%")
                            Log.i(TAG, "Progress: ${progress.toInt()}%")

                        }.addOnCompleteListener { task ->
                            /*val downloadUrl = task.result!!.storage.downloadUrl.result
                            Log.i(TAG, "Image URL : $downloadUrl")
                            iv_profile.tag = downloadUrl
                             loadImage(downloadUrl, iv_profile, R.mipmap.ic_launcher_round)
                            pDialog.dismiss()*/
                        }

                }
                RESULT_CANCELED -> {

                }
                else -> {

                }
            }
        }
    }


}