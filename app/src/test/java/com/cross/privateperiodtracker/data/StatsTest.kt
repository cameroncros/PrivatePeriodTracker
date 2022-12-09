package com.cross.privateperiodtracker.data

import com.cross.privateperiodtracker.data.Stats.Companion.mean
import com.cross.privateperiodtracker.data.Stats.Companion.removeOutliers
import com.cross.privateperiodtracker.data.Stats.Companion.sd
import com.cross.privateperiodtracker.data.Stats.Companion.variance
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsTest {
    @Test
    fun meanTest() {
        val array = ArrayList<Long>(listOf(1, 2, 3, 4, 5));
        assertEquals(3.0, mean(array), 0.01);
    }

    @Test
    fun varianceTest() {
        val array = ArrayList<Long>(listOf(1, 2, 3, 4, 5));
        assertEquals(2.5, variance(array), 0.001);
    }

    @Test
    fun sdTest() {
        val array = ArrayList<Long>(listOf(1, 2, 3, 4, 5));
        assertEquals(1.5811, sd(array), 0.001);
    }

    @Test
    fun removeOutliersTest() {
        val array = ArrayList<Long>(listOf(6, 7, 14, 7, 5, 5, 8, 15))
        val expectedResult = ArrayList<Long>(listOf(5, 5, 6, 7, 7, 8))
        removeOutliers(array)
        assertEquals(expectedResult.size, array.size)
        assertEquals(expectedResult, array)
    }
}