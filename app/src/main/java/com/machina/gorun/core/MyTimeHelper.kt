package com.machina.gorun.core

object MyTimeHelper {

    fun formatMillisToMMSS(timeInMillis: Long): String {
        val totalSecs = timeInMillis / 1000
        val seconds = totalSecs % 60
        val minutes = (totalSecs / 60) % 60
        val hours = totalSecs / 3600
        return if (hours < 1)
            String.format("%02d:%02d", minutes, seconds)
        else
            String.format("%d:%02d:%02d", hours, minutes, seconds)
    }
}