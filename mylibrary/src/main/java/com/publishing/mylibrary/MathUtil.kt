package com.publishing.mylibrary

import kotlin.math.pow
import kotlin.math.sqrt

data class Point(val x: Double, val y: Double)

fun Point.distanceToPoint(point: Point): Double {
    return sqrt((this.x - point.x).pow(2) + (this.y - point.y).pow(2))
}
