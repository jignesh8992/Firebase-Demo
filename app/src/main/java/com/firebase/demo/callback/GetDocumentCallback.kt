package com.firebase.demo.callback

import com.firebase.demo.sociallogin.dao.User

interface GetDocumentCallback {
    fun onGetDocumentSuccess(use: User)
    fun onGetDocumentFailure(errorMessage: String)
}