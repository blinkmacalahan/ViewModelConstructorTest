package com.example.viewmodelconstructortest

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.viewmodelconstructortest.App.Companion.TAG

/**
 * A simple apps that demonstrates you should always pass the constructors into a [ViewModel] because you can't rely on
 * activity / fragment lifecycle setup.
 *
 * To perform this test correctly, you need to simulate a process death that kills the app but still keeps the saved instance state
 * intact. The most reliable way I have found to do this is by changing "Background process limit" in Settings -> System -> Developer options -> Apps
 * and changing its value to "No background processes". This however isn't 100% and you need to verify that when the app / process
 * gets recreated the [MainActivity.onCreate] `savedInstanceState` is NOT null. Use logcat and monitor for the log on ln 22.
 *
 * $ adb logcat -s ViewModelTest
 *
 *
 * To create crash do the following:
 * 1. Change "Background process limit" to "No background processes" (see above).
 * 2. Launch appliaction
 * 3. Press Home to minimize the app
 * 4. Launch another application
 * 5. Use "recents" button to resume app
 * 6. Verify "App onCreate" log occurs AND "MainActivity onCreate: savedInstanceState =" has a non null value.
 * 7. The app should immediately crash
 *
 *
 * Recorded logs on initial launch:
 *      10-15 19:12:57.300  2188  2188 D ViewModelTest: App onCreate
 *      10-15 19:12:57.420  2188  2188 D ViewModelTest: MainActivity onCreate: savedInstanceState = null
 *      10-15 19:12:57.640  2188  2188 D ViewModelTest: DumbFragment init
 *      10-15 19:12:57.640  2188  2188 D ViewModelTest: MainActivity create/get view model
 *      10-15 19:12:57.645  2188  2188 D ViewModelTest: DumbViewModel init
 *      10-15 19:12:57.661  2188  2188 D ViewModelTest: DumbFragment onAttach activity = com.example.viewmodelconstructortest.MainActivity@b56aa0
 *      10-15 19:12:57.661  2188  2188 D ViewModelTest: DumbFragment onCreate
 *
 *
 * Recorded logs on app crash when successfully kill app process but leaving savedInstanceState intact
 *      10-15 19:13:36.364  2786  2786 D ViewModelTest: App onCreate
 *      10-15 19:13:36.487  2786  2786 D ViewModelTest: MainActivity onCreate: savedInstanceState = Bundle[{android:viewHierarchyState=Bundle[mParcelledData.dataSize=528], android:support:fragments=androidx.fragment.app.FragmentManagerState@b7b5659, androidx.lifecycle.BundlableSavedStateRegistry.key=Bundle[EMPTY_PARCEL], android:lastAutofillId=1073741823, android:fragments=android.app.FragmentManagerState@9a9121e}]
 *      10-15 19:13:36.522  2786  2786 D ViewModelTest: DumbFragment init
 *      10-15 19:13:36.527  2786  2786 D ViewModelTest: DumbFragment onAttach activity = com.example.viewmodelconstructortest.MainActivity@1b60aff
 *      10-15 19:13:36.528  2786  2786 D ViewModelTest: DumbFragment onCreate
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "${this::class.java.simpleName} onCreate: savedInstanceState = $savedInstanceState")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(android.R.id.content, DumbFragment()).commit()
        }

        Log.d(TAG, "${this::class.java.simpleName} create/get view model")
        getViewModel {
            DumbViewModel(1)
        }
    }
}

class DumbViewModel(val value: Int) : ViewModel() {
    init {
        Log.d(TAG, "${this::class.java.simpleName} init")
    }
}

class DumbFragment() : Fragment() {
    init {
        Log.d(TAG, "${this::class.java.simpleName} init")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "${this::class.java.simpleName} onAttach activity = $activity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "${this::class.java.simpleName} onCreate")
        ViewModelProvider(requireActivity()).get(DumbViewModel::class.java)
    }
}

/**
 * Boiler plate for creating a ViewModel with a constructor.
 * @param provider A lambda used to create the specific ViewModel instance
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> FragmentActivity.getViewModel(crossinline provider: () -> T) =
    ViewModelProvider(this, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return provider() as T
        }
    }).get(T::class.java)