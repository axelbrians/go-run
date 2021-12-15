package com.machina.gorun.data.models

import androidx.room.ColumnInfo

class JoggingResult(
    val id: Int,

    val distanceTraveled: String,

    val caloriesBurned: String,

    val timeElapsed: String,

    val timeStamp: String,
)