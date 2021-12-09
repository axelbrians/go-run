package com.machina.gorun.data.sources.room


import androidx.room.*
import com.machina.gorun.data.models.Point
import kotlinx.coroutines.flow.Flow

@Dao
interface GoRunDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPoint(point: Point)

    @Query("DELETE FROM point_table")
    suspend fun deletePoints()

    @Query("SELECT * FROM point_table ORDER BY time")
    suspend fun getCurrentPoints(): List<Point>

    @Query("SELECT * FROM point_table ORDER BY time")
    fun getPoints(): Flow<List<Point>>
}