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

val colors : Array<String> = arrayOf("#1abc9c", "#9b59b6", "#e74c3c", "#3498db", "#e67e22")
val scGap : Float = 0.01f
val delay : Long = 30
val rFactor : Float = 3f
val triFactor : Float = 3f
val triColor : Int = Color.WHITE
