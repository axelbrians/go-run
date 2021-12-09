package com.machina.gorun.data.sources.shared_prefs

import android.content.Context
import android.content.SharedPreferences

class LocationSharedPrefs(
    private val context: Context
) {

    companion object {
        const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
        const val FOREGROUND_ENABLED = "is_foreground_enabled"
        const val IS_JOGGING = "is_jogging"

        private const val PREFS_NAME = "GO_RUN_LOC_PREF"
    }

    var prefs: SharedPreferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    /**
     * Returns true if requesting location updates, otherwise returns false.
     */
    fun getLocationTrackingPref(): Boolean {
        return prefs.getBoolean(KEY_FOREGROUND_ENABLED, false)
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(requestingLocationUpdates: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        editor.apply()
    }

    fun setIsJogging(isJogging: Boolean) {
        val oldIsJogging = prefs.getBoolean(IS_JOGGING, false)

        if (oldIsJogging != isJogging) {
            val editor = prefs.edit()
            editor.putBoolean(IS_JOGGING, isJogging)
            editor.apply()
        }
    }

    fun isJogging(): Boolean {
        return prefs.getBoolean(IS_JOGGING, false)
    }
}