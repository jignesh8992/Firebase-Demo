package com.firebase.demo.utilities

import com.firebase.demo.R
import com.google.android.material.textfield.TextInputLayout


fun TextInputLayout.isValidName(): Boolean {
    val name = editText!!.text.toString().trim()
    when {
        name.isEmpty() -> {
            error = context.getString(R.string.error_enter_name)
            return false
        }
        name.length < 2 -> {
            error = context.getString(R.string.error_enter_valid_name)
            return false
        }
        else -> {
            error = null
        }
    }
    return true
}

fun TextInputLayout.isValidEmail(): Boolean {
    val email = editText!!.text.toString().trim()
    when {
        email.isEmpty() -> {
            error = context.getString(R.string.error_enter_email)
            return false
        }
        email.length < 2 -> {
            error = context.getString(R.string.error_enter_valid_email)
            return false
        }
        else -> {
            error = null
        }
    }
    return true
}

fun TextInputLayout.isValidPassword(): Boolean {
    val password = editText!!.text.toString().trim()
    when {
        password.isEmpty() -> {
            error = context.getString(R.string.error_enter_password)
            return false
        }
        password.length < 6 -> {
            error = context.getString(R.string.error_enter_valid_password)
            return false
        }
        else -> {
            error = null
        }
    }
    return true
}

fun TextInputLayout.isValidRepeatPassword(ti_password: TextInputLayout): Boolean {
    val password = ti_password.editText!!.text.toString().trim()
    val repeatPassword = editText!!.text.toString().trim()
    when {
        repeatPassword.isEmpty() -> {
            error = context.getString(R.string.error_enter_repeat_password)
            return false
        }
        repeatPassword != password -> {
            error = context.getString(R.string.error_enter_valid_repeat_password)
            return false
        }
        else -> {
            error = null
        }
    }
    return true
}