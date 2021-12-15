package com.machina.gorun.data.services


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.machina.gorun.core.DefaultDispatchers
import com.machina.gorun.core.MyHelper
import com.machina.gorun.data.models.toPoint
import com.machina.gorun.data.models.toText
import com.machina.gorun.data.repositories.GoRunRepositories
import com.machina.gorun.data.sources.shared_prefs.LocationSharedPrefs
import com.machina.gorun.view.MainActivity
import com.machina.gorun.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundOnlyLocationService : LifecycleService() {

    /*
     * Checks whether the bound activity has really gone away (foreground service with notification
     * created) or simply orientation change (no-op).
     */
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager

    // TODO: Step 1.1, Review variables (no changes).
    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Shared prefs Callback - Called when Preference values is changed
    private lateinit var prefsCallback: SharedPreferences.OnSharedPreferenceChangeListener

    // We save a local reference to last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    // Data store (in this case, Room database) where the service will persist the location data, injected via Hilt
    @Inject lateinit var repository: GoRunRepositories
//
//    @Inject lateinit var homeRepository: HomeRepositoryImpl

    @Inject lateinit var locationPrefs: LocationSharedPrefs

    @Inject lateinit var dispatchers: DefaultDispatchers

    @Inject lateinit var myHelper: MyHelper


    override fun onCreate() {
        super.onCreate()
        Timber.d("LocationService onCreate()")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // TODO: Step 1.2, Review the FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // TODO: Step 1.3, Create a LocationRequest.
        locationRequest = createNewLocationRequest()

//        prefsCallback =
//            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
//                when (key) {
//                    LocationSharedPrefs.IS_JOGGING -> {
//                        val isJogging = sharedPreferences.getBoolean(key, false)
//                        Timber.d("isJogging $isJogging")
//                        if (isJogging) {
//                            subscribeToLocationUpdates()
//                        } else {
//                            unsubscribeToLocationUpdates()
//                            lifecycleScope.launch(dispatchers.default) {
//                                repository.computeJoggingResult()
//                            }
//                        }
//                    }
//                }
//            }
        prefsCallback = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == LocationSharedPrefs.IS_JOGGING) {
                val isJogging = sharedPreferences.getBoolean(key, false)
                Timber.d("isJogging $isJogging")
                if (isJogging) {
                    subscribeToLocationUpdates()
                    lifecycleScope.launch(dispatchers.default) {
                        repository.nukePoints()
                    }
                } else {
                    unsubscribeToLocationUpdates()
                    lifecycleScope.launch(dispatchers.default) {
                        repository.computeJoggingResult()
                    }
                }
            }
        }

        locationPrefs.prefs.registerOnSharedPreferenceChangeListener(prefsCallback)

        // TODO: Step 1.4, Initialize the LocationCallback.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val isLocationOn = myHelper.isLocationActive()
                val isJogging = locationPrefs.isJogging()
                currentLocation = locationResult.lastLocation

//                Timber.d("new Location at ${currentLocation.toLocation().toString()}")
//                Timber.d("isTracingEnabled $isEnabled")
//                Timber.d("isLocation On ${myHelper.isLocationEnabled()}")

                // Notify our Activity that a new location was observed by adding to repository
                if (isLocationOn) {
                    lifecycleScope.launch {
                        currentLocation?.toPoint()?.let {
                            // Update local storage (ROOM) with new location
                            repository.insertPoint(it)
                        }
                    }
                }

                // Updates notification content if this service is running as a foreground
                // service.
                if (serviceRunningInForeground && isJogging) {
//                    Timber.d("new notification")
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(currentLocation)
                    )
                }
            }
        }

        // Infinite Coroutine Loop to Save Current Location to server
        lifecycleScope.launch {
            while (true) {
                currentLocation?.let {
                    val isLocationActive = myHelper.isLocationActive()
                    if (isLocationActive) {
                        // Uncommnet on production
//                        homeRepository.storeTracingLocation(LongLat(it.longitude, it.latitude))
//                            .onEach { }
//                            .launchIn(lifecycleScope)
                    } else if (!isLocationActive && serviceRunningInForeground) {
                        Timber.d("Stopping notification since location is turned off")
                        unsubscribeToLocationUpdates()
                    } else {
//                        Do nothing
                    }
                }
//                myHelper.isLocationEnabled()
                delay(10000L)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand()")

        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)
                ?: false

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        // Tells the system not to recreate the service after it's been killed.
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Timber.d("onBind()")

        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Timber.d("onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange &&
            locationPrefs.getLocationTrackingPref()
        ) {

            Timber.d("Start foreground service")
            val notification = generateNotification(currentLocation)
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onDestroy() {
        Timber.d("onDestroy()")
        locationPrefs.prefs.unregisterOnSharedPreferenceChangeListener(prefsCallback)
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        Timber.d("subscribeToLocationUpdates()")

        locationPrefs.saveLocationTrackingPref( true)

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, ForegroundOnlyLocationService::class.java))

        try {
            // TODO: Step 1.5, Subscribe to location changes.
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            locationPrefs.saveLocationTrackingPref(false)
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Timber.d("unsubscribeToLocationUpdates()")

        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Location Callback removed.")
                    stopSelf()
                } else {
                    Timber.d("Failed to remove Location Callback.")
                }
            }
            locationPrefs.saveLocationTrackingPref( false)
        } catch (unlikely: SecurityException) {
            locationPrefs.saveLocationTrackingPref( true)
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun createNewLocationRequest(): LocationRequest {
        val updateInterval = 5L
        return LocationRequest.create().apply {

            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.SECONDS.toMillis(updateInterval)


            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.SECONDS.toMillis(1)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = TimeUnit.SECONDS.toMillis(updateInterval)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    private fun generateNotification(location: Location?): Notification {
        Timber.d("generateNotification()")

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val mainNotificationText = location?.toText() ?: "Current point not found."
        val titleText = "Current point"
        val titleTextNotif = "GoRun"

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_LOW
            )

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val cancelIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, PendingIntent.FLAG_IMMUTABLE or 0
        )

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setVibrate(LongArray(1))
            .setContentTitle(titleTextNotif)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.drawable.ic_run)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .addAction(
//                R.drawable.ic_map_location,
//                "Open",
//                activityPendingIntent
//            )
//            .addAction(
//                R.drawable.ic_arrow_back,
//                "Stop",
//                servicePendingIntent
//            )
            .setContentIntent(activityPendingIntent)
            .build()
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: ForegroundOnlyLocationService
            get() = this@ForegroundOnlyLocationService
    }

    companion object {
        private const val PACKAGE_NAME = "its.telemedicine.digihealth"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 11345121

        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_02"
    }
}