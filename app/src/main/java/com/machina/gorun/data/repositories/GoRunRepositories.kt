package com.machina.gorun.data.repositories

import android.location.Location
import com.machina.gorun.data.models.Point
import com.machina.gorun.data.models.toFourDecimal
import com.machina.gorun.data.sources.room.GoRunDao
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class GoRunRepositories @Inject constructor(
    private val goRunDao: GoRunDao
) {

    suspend fun nukePoints() {
        goRunDao.deletePoints()
    }

    suspend fun insertPoint(point: Point) {
        val points = goRunDao.getCurrentPoints()
        val result = floatArrayOf(0f, 0f, 0f, 0f, 0f)
        // the distance metric is in KM
        if (points.isNotEmpty()) {
            val last = points.last()
            Location.distanceBetween(
                last.latitude,
                last.longitude,
                point.latitude,
                point.longitude,
                result)

            val delta = result[0].toDouble().toFourDecimal()
            val old = last.distanceInMeter

            point.distanceInMeter = old + delta

            Timber.d("last value $old")
            Timber.d("delta $delta")

//            Timber.d("${last.latitude} ${last.longitude}  ${point.latitude} ${point.longitude} ${result[0]}")
        }
        Timber.d("calculated distance in m ${result[0]}")
        Timber.d("current distance in m ${point.distanceInMeter}")

        goRunDao.insertPoint(point)
    }



    suspend fun computeJoggingResult() {
        var res = goRunDao.getCurrentPoints()
//        Timber.d("Collected points $res")

        goRunDao.deletePoints()
        res = goRunDao.getCurrentPoints()
//        Timber.d("Collected points $res")
    }

    fun getPoints(): Flow<List<Point>> {
        return goRunDao.getPoints()
    }
}