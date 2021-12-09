package com.machina.gorun.data.sources.room


import androidx.room.Database
import androidx.room.RoomDatabase
import com.machina.gorun.data.models.JoggingResult
import com.machina.gorun.data.models.Point

@Database(entities = [(Point::class), (JoggingResult::class)], version = 2)
abstract class GoRunDatabase : RoomDatabase() {

    abstract fun goRunDao(): GoRunDao

}