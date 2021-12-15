package com.machina.gorun.data.repositories

import android.location.Location
import com.machina.gorun.data.models.*
import com.machina.gorun.data.sources.room.GoRunDao
import com.machina.gorun.data.sources.shared_prefs.UserSharedPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import kotlin.text.Typography.less
import kotlin.text.Typography.lessOrEqual

class GoRunRepositories @Inject constructor(
    private val goRunDao: GoRunDao,
    private val prefs: UserSharedPrefs
) {

    suspend fun nukePoints() {
        goRunDao.deletePoints()
    }

    suspend fun insertPoint(point: Point) {
        //{(Weight in kg * MET * 3.5) / 200} * time in minute

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

            val distanceDifference = result[0].toDouble().toFourDecimal()


            point.distanceInMeter = distanceDifference + last.distanceInMeter

//            Timber.d("last value ${point.distanceInMeter}")
//            Timber.d("distanceDifference $distanceDifference")


            val timeDifference = (point.time - last.time) / 1000
            val met = (distanceDifference.meterToMile() / timeDifference.secondToHour()).toMET()
            val addedCalories = (((prefs.getUserWeight() * met * 3.5) / 200) *
                    timeDifference.secondToMinute()).toFourDecimal()

            point.calories = last.calories.toFourDecimal() + addedCalories
            Timber.d("MET $met")
            Timber.d("last calories ${last.calories}")
            Timber.d("added calories $addedCalories")


//            Timber.d("${last.latitude} ${last.longitude}  ${point.latitude} ${point.longitude} ${result[0]}")
        }
//        Timber.d("calculated distance in m ${result[0]}")
//        Timber.d("current distance in m ${point.distanceInMeter}")

        goRunDao.insertPoint(point)
    }

    fun getPoints(): Flow<List<Point>> {
        return goRunDao.getPoints()
    }

    suspend fun computeJoggingResult() {
        val points = goRunDao.getCurrentPoints()

        val timeElapsed = points.last().time -
                points.first().time
        if (points.isNotEmpty()) {
            val joggingResult = JoggingResultDto(
                distanceTraveled = points.last().distanceInMeter,
                caloriesBurned = points.last().calories,
                timeElapsed = timeElapsed,
                timeStamp = points.first().time
            )

            goRunDao.insertJoggingResult(joggingResult)
        }

        goRunDao.deletePoints()
    }

    fun getJoggingResults(): Flow<List<JoggingResult>> {
        return goRunDao.getJoggingResults().map { resultsDto ->
            resultsDto.map { it.toJoggingResult() }
        }
    }

    private fun Double.toMET(): Double {
        return if (this == 0.0) {
            0.0
        } else if (this <= 4) {
            5.0
        } else if (this <= 4.8) {
            8.0
        } else if (this <= 5) {
            8.3
        } else if (this <= 5.2) {
            9.0
        } else if (this <= 6) {
            9.8
        } else if (this <= 6.2) {
            10.0
        } else if (this <= 6.7) {
            10.5
        } else if (this <= 7) {
            11.0
        } else if (this <= 7.5) {
            11.5
        } else if (this <= 8) {
            11.8
        } else if (this <= 8.6) {
            12.3
        } else if (this <= 9) {
            12.8
        } else if (this <= 9.7) {
            13.3
        } else if (this <= 10) {
            14.5
        } else if (this <= 11) {
            16.0
        } else if (this <= 12) {
            19.0
        } else if (this <= 13) {
            19.8
        } else if (this <= 14 || this > 14) {
            23.0
        } else {
            4.5
        }
    }

    private fun Long.secondToMinute(): Double {
        return this / SECOND_TO_MINUTE_RATIO
    }

    private fun Long.secondToHour(): Double {
        return this / SECOND_TO_HOUR_RATIO
    }

    private fun Double.meterToMile(): Double {
        return (this / 1000) / KM_TO_MILE_RATIO
    }

    companion object {
        private const val SECOND_TO_MINUTE_RATIO = 60.0
        private const val SECOND_TO_HOUR_RATIO = 3600.0
        private const val KM_TO_MILE_RATIO = 1.609344
    }
}