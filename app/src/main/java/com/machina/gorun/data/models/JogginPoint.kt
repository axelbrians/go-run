package com.machina.gorun.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "jogging_point_table")
data class JoggingPoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val joggingResultId: Int,
    val time: Long,
    val latitude: Double,
    val longitude: Double,
)