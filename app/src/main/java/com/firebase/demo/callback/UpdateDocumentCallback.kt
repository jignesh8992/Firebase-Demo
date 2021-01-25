package com.firebase.demo.callback

interface UpdateDocumentCallback {
    fun onUpdateDocumentSuccess()
    fun onUpdateDocumentFailure(errorMessage: String)
}