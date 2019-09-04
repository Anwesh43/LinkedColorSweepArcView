package com.anwesh.uiprojects.colorsweeparcview

/**
 * Created by anweshmishra on 04/09/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path

val colors : Array<String> = arrayOf("#1abc9c", "#9b59b6", "#e74c3c", "#3498db", "#e67e22")
val scGap : Float = 0.01f
val delay : Long = 30
val rFactor : Float = 3f
val triFactor : Float = 3f
val triColor : Int = Color.WHITE

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n))

fun Canvas.drawTriangle(size : Float, sc1 : Float, sc2 : Float, paint : Paint) {
    paint.color = triColor
    save()
    scale(sc2, sc2)
    val path : Path = Path()
    path.moveTo(-size, size - 2 * size * sc1)
    path.lineTo(0f, -size)
    path.lineTo(size, size - 2 * size * sc1)
    drawPath(path, paint)
    restore()
}

fun Canvas.drawArc(i : Int, size : Float, sc1 : Float, sc2 : Float, paint : Paint) {
    paint.color = Color.parseColor(colors[i])
    var sweepDeg : Float = 360f * (1 - sc1)
    if (sc2 > 0f) {
        sweepDeg = 360f * sc2
    }
    drawArc(RectF(-size, -size, size, size), 360f * sc1, sweepDeg, true, paint)
}

fun Canvas.drawCSANode(i : Int, scale : Float, sc : Float, paint : Paint) : Float {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val size : Float = Math.min(w, h) / rFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    save()
    translate(w / 2, h / 2)
    drawArc(i, size, sc2, sc, paint)
    drawTriangle(size, sc1, sc, paint)
    restore()
    return sc2
}

class ColorSweepArcView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}