package com.cross.privateperiodtracker.utils

import androidx.test.espresso.NoMatchingViewException

class Utils {
    companion object {
        fun performActionCount(
            action: () -> Unit,
            maxRepeatTimes: Int = -1
        ) {
            var success = false
            var counter = if (maxRepeatTimes == -1) Int.MIN_VALUE else 0
            while (!success && counter < maxRepeatTimes) {
                success = try {
                    counter++
                    action()
                    true
                } catch (e: NoMatchingViewException) {
                    false
                }
            }
        }
    }
}