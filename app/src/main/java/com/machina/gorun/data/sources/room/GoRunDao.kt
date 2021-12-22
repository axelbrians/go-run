package com.machina.gorun.data.sources.room


import androidx.room.*
import com.machina.gorun.data.models.JoggingPoint
import com.machina.gorun.data.models.JoggingResultDto
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJoggingResult(result: JoggingResultDto)

    @Query("SELECT * FROM jogging_result_table ORDER BY timestamp DESC")
    fun getJoggingResults(): Flow<List<JoggingResultDto>>

    @Query("SELECT * FROM jogging_result_table ORDER BY timestamp DESC")
    suspend fun getJoggingResultsNoFlow(): List<JoggingResultDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllJoggingPoints(points: List<JoggingPoint>)

    @Query("SELECT * FROM jogging_point_table WHERE joggingResultId = :id")
    suspend fun getJoggingPointsNoFlow(id: Int): List<JoggingPoint>
}