package com.cross.privateperiodtracker

import android.content.SharedPreferences

class SettingsManager {
    companion object {
        fun checkNotificationEnabled(prefs: SharedPreferences): List<Boolean> {
            return listOf(
                prefs.getBoolean("day0", true),
                prefs.getBoolean("day1", true),
                prefs.getBoolean("day2", true),
                prefs.getBoolean("day3", true),
                prefs.getBoolean("day4", true),
                prefs.getBoolean("day5", true),
                prefs.getBoolean("day6", true),
                prefs.getBoolean("day7", true),
            )
        }

        fun setNotificationEnabled(prefs: SharedPreferences, enabled: List<Boolean>) {
            prefs.edit()
                .putBoolean("day0", enabled[0])
                .putBoolean("day1", enabled[1])
                .putBoolean("day2", enabled[2])
                .putBoolean("day3", enabled[3])
                .putBoolean("day4", enabled[4])
                .putBoolean("day5", enabled[5])
                .putBoolean("day6", enabled[6])
                .putBoolean("day7", enabled[7])
                .apply()
        }

        fun checkAutoEndPeriod(prefs: SharedPreferences): Boolean {
            return prefs.getBoolean("auto_end_period", false)
        }

        fun setAutoEndPeriod(prefs: SharedPreferences, b: Boolean) {
            return prefs.edit().putBoolean("auto_end_period", b).apply()
        }

        fun getAutoEndPeriodDays(prefs: SharedPreferences): Long {
            return prefs.getLong("auto_end_period_days", 7)
        }

        fun setAutoEndPeriodDays(prefs: SharedPreferences, i: Long) {
            return prefs.edit().putLong("auto_end_period_days", i).apply()
        }
    }
}