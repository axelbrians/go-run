package com.machina.gorun.core

object MyTimeHelper {

    fun formatMillisToMMSS(timeInMillis: Long): String {
        val totalSecs = timeInMillis / 1000
        val hours = totalSecs % 3600
        val minutes = hours / 60
        val seconds = totalSecs % 60
        return if (hours > 1)
            String.format("%02d:%02d", minutes, seconds)
        else
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}