package com.firebase.demo.utilities

import android.view.View
import com.google.android.material.textfield.TextInputLayout

// [START disableEditText]
fun TextInputLayout.disableEditText() {
    isFocusable = false
    isFocusableInTouchMode = false
    isClickable = false
}
// [END disableEditText]


// [START disableSpinner]
fun View.disableSpinner() {
    isFocusable = false
    isFocusableInTouchMode = false
    isClickable = false
    isEnabled = false
}
// [END disableSpinner]