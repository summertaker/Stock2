package com.summertaker.stock2

class Calculator {
    private val MILLISECONDS: Long = 1000
    private val SECONDS: Long = 60
    private val MINUTES: Long = 60

    fun formatMillisIntoHumanReadable(t: Long): String? {
        var time = t
        time /= MILLISECONDS
        val seconds = (time % SECONDS).toInt()
        time /= SECONDS
        val minutes = (time % MINUTES).toInt()
        time /= MINUTES
        val hours = (time % 24).toInt()
        val days = (time / 24).toInt()
        return if (days == 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(
                "%dd%d:%02d:%02d", days, hours, minutes,
                seconds
            )
        }
    }
}