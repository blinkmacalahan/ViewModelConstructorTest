package com.example.viewmodelconstructortest

import android.app.Application
import android.util.Log

class App: Application() {

    companion object {
        const val TAG = "ViewModelTest"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "App onCreate")
    }
}