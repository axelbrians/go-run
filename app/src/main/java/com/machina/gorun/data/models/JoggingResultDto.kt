package com.machina.gorun.data.models

import android.icu.util.Calendar
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.machina.gorun.core.MyTimeHelper
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "jogging_result_table")
data class JoggingResultDto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "distance_traveled")
    val distanceTraveled: Double,

    @ColumnInfo(name = "calories_burdned")
    val caloriesBurned: Double,

    @ColumnInfo(name = "time_elapsed")
    val timeElapsed: Long,

    @ColumnInfo(name = "timestamp")
    val timeStamp: Long,
)


private val locale: Locale = Locale("en", "en_GB")
private val sdf = SimpleDateFormat("dd MMM yyyy", locale)
private val calendar = Calendar.getInstance()

fun JoggingResultDto.toJoggingResult(): JoggingResult {
    val distanceTraveled = "${String.format("%.1f", this.distanceTraveled)} M"

    val timeElapsed = MyTimeHelper.formatMillisToMMSS(this.timeElapsed)
    calendar.timeInMillis = this.timeStamp
    val timeStamp = sdf.format(calendar.time)

    return JoggingResult(
        id = id,
        distanceTraveled = distanceTraveled,
        caloriesBurned = caloriesBurned.toString(),
        timeElapsed = timeElapsed,
        timeStamp = timeStamp
    )
}