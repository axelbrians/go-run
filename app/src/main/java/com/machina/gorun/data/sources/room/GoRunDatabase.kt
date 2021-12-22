package com.machina.gorun.data.sources.room


import androidx.room.Database
import androidx.room.RoomDatabase
import com.machina.gorun.data.models.JoggingPoint
import com.machina.gorun.data.models.JoggingResultDto
import com.machina.gorun.data.models.Point

@Database(entities = [
    (Point::class),
    (JoggingResultDto::class),
    (JoggingPoint::class)],
    version = 6)
abstract class GoRunDatabase : RoomDatabase() {

    abstract fun goRunDao(): GoRunDao

}