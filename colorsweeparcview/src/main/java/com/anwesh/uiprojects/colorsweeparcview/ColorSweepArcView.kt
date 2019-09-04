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
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CSANode(var i : Int, val state : State = State()) {

        private var next : CSANode? = null
        private var prev : CSANode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = CSANode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, sc : Float, paint : Paint) {
            val sck : Float = canvas.drawCSANode(i, state.scale, sc, paint)
            if (sck > 0f) {
                next?.draw(canvas, sck, paint)
            }
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CSANode {
            var curr : CSANode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ColorSweepArc(var i : Int) {

        private val root : CSANode = CSANode(0)
        private var curr : CSANode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, 0f, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ColorSweepArcView) {

        private val csa : ColorSweepArc = ColorSweepArc(0)
        private var animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            csa.draw(canvas, paint)
            animator.animate {
                csa.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            csa.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ColorSweepArcView {
            val view : ColorSweepArcView = ColorSweepArcView(activity)
            activity.setContentView(view)
            return view
        }
    }
}