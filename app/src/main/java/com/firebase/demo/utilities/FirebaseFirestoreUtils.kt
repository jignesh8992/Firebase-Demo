package com.firebase.demo.utilities

import android.app.Activity
import android.util.Log
import com.example.jdrodi.utilities.toast
import com.firebase.demo.callback.AddUserCallback
import com.firebase.demo.callback.DeleteCallback
import com.firebase.demo.callback.GetDocumentCallback
import com.firebase.demo.callback.UpdateDocumentCallback
import com.firebase.demo.sociallogin.dao.User
import com.firebase.demo.sociallogin.dao.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

/**
 * FirestoreUtils - Helper method of firestore.
 * @author:  Jignesh N Patel
 * @date: 22-Jan-2021 4:49 PM
 */

private const val TAG = "FirebaseFirestoreUtils"

// [START getFStore]
/**
 * TODO.. Instance of firebase firestore
 *
 * @return
 */
fun getFStore(): FirebaseFirestore {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    return FirebaseFirestore.getInstance()
}
// [END getFStore]


// [START checkAndInsertDocument]
/**
 * TODO.. Check if document is already exist
 *
 * @param userProfile
 * @param addUserCallback
 */
fun checkAndInsertDocument(userProfile: UserProfile, addUserCallback: AddUserCallback) {
    Log.i(TAG, Throwable().stackTrace[0].methodName)
    val docRef = getFStore().collection(COLLECTION_USER).document(getUId())

    // Before add any document make sure if it is exist or not
    docRef.get().addOnSuccessListener {
        if (it.exists()) {
            // Document already exist no need to add it again
            Log.i(TAG, "Document already exist: " + getUId())
            addUserCallback.onUserExist(userProfile)
        } else {
            // Document not exist just add it
            Log.i(TAG, "Document not exist: " + getUId())
            insertDocument(userProfile, addUserCallback)
        }
    }
}
// [END checkAndInsertDocument]

// [START insertDocument]
/**
 * TODO.. Add new document
 *
 * @param userProfile
 * @param addUserCallback
 */
fun insertDocument(userProfile: UserProfile, addUserCallback: AddUserCallback) {
    Log.d(TAG, "insertDocument")
    val docRef = getFStore().collection(COLLECTION_USER).document(getUId())
    val user: HashMap<String, Any> = HashMap()
    user[KEY_NAME] = userProfile.displayName!!
    user[KEY_EMAIL] = userProfile.email!!
    user[KEY_PROFILE] = userProfile.photoUrl!!.toString()
    Log.d(TAG, "success: photoUrl->" + userProfile.photoUrl!!)
    docRef.set(user).addOnCompleteListener { result ->
        when {
            result.isSuccessful -> {
                Log.i(TAG, "Document successfully added: " + getUId())
                addUserCallback.onUserAddedSuccess(userProfile)
            }
            else -> {
                Log.i(TAG, "Document add failure: " + result.exception.toString())
                addUserCallback.onUserAddFailure(result.exception.toString())
            }
        }
    }
}
// [END insertDocument]


// [START getDocument]
/**
 * TODO.. Retrieve document
 *
 * @param getDocumentCallback
 */
fun Activity.getDocument(getDocumentCallback: GetDocumentCallback) {
    val userId = getUId()
    val docRef = getFStore().collection(COLLECTION_USER).document(userId)
    docRef.addSnapshotListener(this) { value, error ->
        if (value != null) {
            val name = value.getString(KEY_NAME)
            val email = value.getString(KEY_EMAIL)
            val profile = value.getString(KEY_PROFILE)
            val user = User(name, email, profile)
            getDocumentCallback.onGetDocumentSuccess(user)

        } else {
            toast(error!!.localizedMessage)
            getDocumentCallback.onGetDocumentFailure(error!!.localizedMessage.toString())
        }
    }
}
// [END getDocument]


// [START updateDocument]
/**
 * TODO Upload Document
 *
 * @param user
 * @param updateDocumentCallback
 */
fun updateDocument(user: User, updateDocumentCallback: UpdateDocumentCallback) {
    val docRef = getFStore().collection(COLLECTION_USER).document(getUId())
    val fUser: HashMap<String, Any> = HashMap()
    fUser[KEY_NAME] = user.displayName!!
    fUser[KEY_EMAIL] = user.email!!
    fUser[KEY_PROFILE] = user.photoUrl!!
    docRef.update(fUser).addOnCompleteListener { result ->
        when {
            result.isSuccessful -> {
                updateDocumentCallback.onUpdateDocumentSuccess()
            }
            else -> {
                val error = result.exception.toString()
                updateDocumentCallback.onUpdateDocumentFailure(error)
            }
        }
    }
}
// [END updateDocument]


// [START deleteDocument]
/**
 * TODO.. Delete the document
 *
 * @param deleteCallback
 */
fun deleteDocument(deleteCallback: DeleteCallback) {
    val docRef = getFStore().collection(COLLECTION_USER).document(getUId())
    docRef.delete().addOnCompleteListener { result ->
        when {
            result.isSuccessful -> {
                deleteAccount(deleteCallback)
            }
            else -> {
                deleteCallback.onDeleteDocumentFailure(result.exception.toString())
            }
        }
    }
}
// [END deleteDocument]