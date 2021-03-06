package com.machina.gorun.data.sources.shared_prefs

import android.content.Context
import android.content.SharedPreferences

class UserSharedPrefs(
    private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "GO_RUN_USER_PREF"
        private const val USER_WEIGHT = "user_weight"
        private const val USER_HEIGHT = "user_height"
    }

     private val prefs: SharedPreferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun getUserWeight(): Double {
        return prefs.getFloat(USER_WEIGHT, 60F).toDouble()
    }

    fun setUserWeight(weight: Double) {
        with(prefs.edit()) {
            putFloat(USER_WEIGHT, weight.toFloat())
            apply()
        }
    }

    fun getUserHeight(): Double {
        return prefs.getFloat(USER_HEIGHT, 150F).toDouble()
    }

    fun setUserHeight(weight: Double) {
        with(prefs.edit()) {
            putFloat(USER_HEIGHT, weight.toFloat())
            apply()
        }
    }
}