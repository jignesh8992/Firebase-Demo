package com.firebase.demo.callback

import com.firebase.demo.sociallogin.dao.User

interface DeleteCallback {
    fun onDeleteDocumentSuccess()
    fun onDeleteDocumentFailure(errorMessage: String)
}