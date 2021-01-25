package com.firebase.demo.callback

interface StorageUploadCallback {
    fun onUploadSuccess(downloadUrl: String)
    fun onUploadFailure(errorMessage: String)
    fun onUploadProgress(progress: Int)
}