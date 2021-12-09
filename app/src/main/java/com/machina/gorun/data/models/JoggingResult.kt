package com.machina.gorun.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jogging_result_table")
data class JoggingResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "distance_traveled")
    val distanceTraveled: Double,

    @ColumnInfo(name = "calories_burdned")
    val caloriesBurned: Double,

    @ColumnInfo(name = "time_elapsed")
    val timeElapsed: Double,

    @ColumnInfo(name = "timestamp")
    val timeStamp: Long,

)
