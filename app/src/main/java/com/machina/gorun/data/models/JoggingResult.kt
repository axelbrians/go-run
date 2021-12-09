package com.machina.gorun.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jogging_result_table")
data class JoggingResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "distance_traveled")
    val distanceTraveled: String,

    @ColumnInfo(name = "calories_burdned")
    val caloriesBurned: Double,

    @ColumnInfo(name = "time_elapsed")
    val timeElapsed: String,

    @ColumnInfo(name = "date")
    val date: String

)
