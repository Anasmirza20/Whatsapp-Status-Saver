package com.whatsappstatus.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateManager {
    fun Date.getTimeAgo(format: String = "yyyy-MM-dd'T'HH:mm:ss.SSS"): String? {
        kotlin.runCatching {
            val date = SimpleDateFormat(format, Locale.getDefault())
            val calendar = Calendar.getInstance()
            val time = utcToLocal(this)
            if (time != null)
                calendar.time = time

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val currentCalendar = Calendar.getInstance()

            val currentYear = currentCalendar.get(Calendar.YEAR)
            val currentMonth = currentCalendar.get(Calendar.MONTH)
            val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
            val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentCalendar.get(Calendar.MINUTE)

            return if (year < currentYear) {
                val interval = currentYear - year
                if (interval == 1) "$interval year ago" else "$interval years ago"
            } else if (month < currentMonth) {
                val interval = currentMonth - month
                if (interval == 1) "$interval month ago" else "$interval months ago"
            } else if (day < currentDay) {
                val interval = currentDay - day
                if (interval == 1) "$interval day ago" else "$interval days ago"
            } else if (hour < currentHour) {
                val interval = currentHour - hour
                if (interval == 1) "$interval hour ago" else "$interval hours ago"
            } else if (minute < currentMinute) {
                val interval = currentMinute - minute
                if (interval == 1) "$interval minute ago" else "$interval minutes ago"
            } else {
                "a moment ago"
            }
        }.onFailure {
            Log.i(javaClass.name, "getTimeAgo: ${it.message}")
        }
        return null
    }

    private fun utcToLocal(date: Date?) =
        date?.time?.plus(
            TimeZone.getTimeZone(Calendar.getInstance().timeZone.id).getOffset(date.time)
        )
            ?.let {
                Date(it)
            }
}