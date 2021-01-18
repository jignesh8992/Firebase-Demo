package com.firebase.demo.utilities

import android.view.View
import com.example.jdrodi.widgets.MaterialSpinner
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


fun TextInputLayout.disableEditText() {
    isFocusable = false
    isFocusableInTouchMode = false
    isClickable = false
}

fun View.disableSpinner() {
    isFocusable = false
    isFocusableInTouchMode = false
    isClickable = false
    isEnabled = false
}