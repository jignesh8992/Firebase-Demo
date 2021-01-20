package com.firebase.demo

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk


open class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        if(FacebookSdk.isInitialized()){
            Log.i("TAG","Already_initialized")
        }else{
            Log.i("TAG","Not_initialized")
            FacebookSdk.fullyInitialize()
        }

    }
}