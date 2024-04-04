package com.publishing.mylibrary

import kotlin.math.pow
import kotlin.math.sqrt

data class PointExt(val x: Double, val y: Double)

fun PointExt.distanceToPointExt(point: PointExt): Double {
    return sqrt((this.x - point.x).pow(2) + (this.y - point.y).pow(2))
}
