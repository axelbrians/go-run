package com.machina.gorun.core

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity

class MyHelper(
    private val context: Context
) {

    fun showToast(message: String, length: Int) {
        Toast.makeText(context, message, length).show()
    }

    fun showQuickToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun hideSoftKeyboard(fragmentActivity: FragmentActivity) {
        if (fragmentActivity.currentFocus == null) {
            return
        }
        val inputMethodManager =
            fragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(fragmentActivity.currentFocus?.windowToken, 0)
    }

    fun hideSoftKeyboard(view: View, mContext: Context) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun isLocationActive(): Boolean {
        val res = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This was deprecated in API 28
            val mode: Int =
                Settings.Secure.getInt(
                    context.contentResolver, Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
                )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }

//        Timber.d("Location isEnabled $res")
        return res
    }
}