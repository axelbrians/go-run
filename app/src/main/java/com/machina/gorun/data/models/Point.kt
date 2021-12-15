package com.machina.gorun.data.models

import android.location.Location
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.machina.gorun.core.MyTimeHelper

@Entity(tableName = "point_table")
data class Point(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val time: Long,
    val latitude: Double,
    val longitude: Double,
    var distanceInMeter: Double,
    var calories: Double
)


fun Location?.toPoint(): Point? {
    return if (this != null) {
        Point(
            time = this.time,
            latitude = this.latitude.toFourDecimal(),
            longitude = this.longitude.toFourDecimal(),
            distanceInMeter = 0.0,
            calories = 0.0
        )
    } else {
        null
    }
}

fun Double.toFourDecimal(): Double {
    return String.format("%.4f", this).toDouble()
}

fun Double.toTwoDecimal(): Double {
    return String.format("%.2f", this).toDouble()
}

fun Point?.toText(): String {
    return if (this != null) {
        val timeElapsed = MyTimeHelper.formatMillisToMMSS(this.time)

        "${this.distanceInMeter.toTwoDecimal()} meters   |   ${this.calories.toTwoDecimal()} cal   |   $timeElapsed"
    } else {
        "Data is not found"
    }
}

fun Location?.toText(): String {
    return if (this != null) {
        toParenthesesString(latitude, longitude)
    } else {
        "Unknown location"
    }
}

fun toParenthesesString(lat: Double, lon: Double): String {
    return "Lat : $lat   |   Lon : $lon"
}