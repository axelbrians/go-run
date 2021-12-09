package com.machina.gorun.view

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.machina.gorun.BuildConfig
import com.machina.gorun.data.services.ForegroundOnlyLocationService
import com.machina.gorun.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment

    var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    private lateinit var prefsCallback: SharedPreferences.OnSharedPreferenceChangeListener

    // Monitors connection to the while-in-use service.
    val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.d("location service connected")
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.d("location service disconnected")
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()

        val result = floatArrayOf(0f, 0f, 0f, 0f, 0f)

        Location.distanceBetween(0.0, 0.0, 1.0, 1.0, result)

        // the distance metric is in KM
        Timber.d("result array ${result[0]} ${result[1]}")
    }

    private fun setupView() {
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isForegroundPermissionApproved()) {
            requestForegroundPermissions()
        }
    }

    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    fun requestForegroundPermissions() {
        val provideRationale = isForegroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            showRequestAccessSnackBar(binding.root)
        } else {
            Timber.d("Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d("onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Timber.d("User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    showOpenAppSettingsSnackBar(binding.root)
                }
            }
        }
    }

    fun startTracking() {
        foregroundOnlyLocationService?.subscribeToLocationUpdates()

        if (foregroundOnlyLocationService != null) {
            Timber.d("Start observing to location updates")
        } else {
            Timber.d("Service Not Bound")
        }

//        viewModel.getLocation()
    }

    // TODO: Step 1.0, Review Permissions: Method checks if permissions approved.
    fun isForegroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun showRequestAccessSnackBar(view: View) {
        Snackbar.make(
            view,
            "Location permission needed for Track jogging functionality",
            Snackbar.LENGTH_LONG
        )
            .setAction("Ok") {
                // Request permission
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                )
            }
            .show()
    }

    fun showOpenAppSettingsSnackBar(view: View) {
        Snackbar.make(
            view,
            "Location access denied, Track jogging feature will be disabled.",
            Snackbar.LENGTH_LONG
        )
            .setAction("Setting") {
                // Build intent that displays the App settings screen.
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID,
                    null
                )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .show()
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)

        lifecycleScope.launch {
            while (true) {
                if (foregroundOnlyLocationService != null) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                    Timber.d("Start observing to location updates")
                    break
                } else {
                    Timber.d("Service Not Bound")
                }

                delay(2000L)
            }
        }
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            Timber.d("unbind location service")
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }

        super.onStop()
    }

    companion object {
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }
}