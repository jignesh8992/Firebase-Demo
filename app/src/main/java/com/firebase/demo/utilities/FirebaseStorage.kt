package com.firebase.demo.utilities

import android.net.Uri
import android.util.Log
import com.firebase.demo.callback.StorageUploadCallback
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

/**
 * FirebaseStorage.kt - Helper method of Firebase Storage.
 * @author:  Jignesh N Patel
 * @date: 23-Jan-2021 4:32 PM
 */

private const val TAG = "FirebaseStorage"
// [START getStorageRef]
/**
 * TODO.. Instance of the StorageReference
 *
 * @return
 */
fun getStorageRef(): StorageReference {
    return FirebaseStorage.getInstance().reference
}
// [END getStorageRef]


// [START uploadFile]
/**
 * TODO.. Upload file to the fire storage and retrieve download url
 *
 * @param path
 * @param storageUploadCallback
 */
fun uploadFile(path: String, storageUploadCallback: StorageUploadCallback) {
    val file: Uri = Uri.fromFile(File(path))
    val imageRef: StorageReference = getStorageRef().child("images/" + getUId())
    imageRef.putFile(file)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                Log.i(TAG, "Image URL : $downloadUrl")
                storageUploadCallback.onUploadSuccess(downloadUrl)
            }
        }
        .addOnFailureListener { error ->
            val errorString = error.localizedMessage.toString()
            Log.e(TAG, "Error occurred! : $errorString")
            storageUploadCallback.onUploadFailure(errorString)
        }.addOnProgressListener { taskSnapshot ->
            val progress: Double = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            Log.i(TAG, "Progress: ${progress.toInt()}%")
            storageUploadCallback.onUploadProgress(progress.toInt())

        }
}
// [END uploadFile]