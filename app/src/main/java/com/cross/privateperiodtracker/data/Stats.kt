package com.cross.privateperiodtracker.data

import kotlin.math.sqrt

class Stats {
    companion object {
        fun removeOutliers(numbers: ArrayList<Long>) {
            if (numbers.size == 0) return

            // Sort numbers.
            numbers.sort()

            // Remove all numbers greater than 1.5 * the mean.
            val cutoff = mean(numbers) * 1.5
            while (numbers[numbers.size - 1] > cutoff) {
                numbers.removeAt(numbers.size - 1)
            }
        }

        fun mean(numbers: ArrayList<Long>): Double {
            var mean = 0L
            for (num in numbers) {
                mean += num
            }
            return mean.toDouble() / numbers.size
        }

        fun variance(numbers: ArrayList<Long>): Double {
            val mean = mean(numbers)
            var sum = 0.0
            for (num in numbers) {
                sum += (num - mean) * (num - mean)
            }
            return sum / (numbers.size - 1)
        }

        fun sd(numbers: ArrayList<Long>): Double {
            return sqrt(variance(numbers))
        }
    }

}