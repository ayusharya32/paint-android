package com.appsbyayush.paintspace.utils

import android.util.Log
import android.view.MotionEvent
import kotlin.math.atan2

class UserGestureDetector(
    private val gestureEventListener: GestureEventListener
) {
    companion object {
        private const val TAG = "CustomGestureDetecyyy"
    }

    private var touchStartX = 0.0F
    private var touchStartY = 0.0F

    private var touchMoveX = 0.0F
    private var touchMoveY = 0.0F

    private var secondTouchStartX = 0.0F
    private var secondTouchStartY = 0.0F

    private var secondTouchMoveX = 0.0F
    private var secondTouchMoveY = 0.0F

    private var secondPointerStarted = false

    private var firstPointerId: Int = -1
    private var secondPointerId: Int = -1

    fun onTouchEvent(event: MotionEvent) {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onMotionEventActionDown(event)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                onMotionEventActionPointerDown(event)
            }

            MotionEvent.ACTION_MOVE -> {
                onMotionEventActionMove(event)
            }

            MotionEvent.ACTION_POINTER_UP ->{
                onMotionEventActionPointerUp(event)
            }

            MotionEvent.ACTION_UP -> {
                onMotionEventActionUp(event)
            }
        }
    }

    private fun onMotionEventActionDown(event: MotionEvent) {
        Log.d(TAG, "onMotionEventActionDown: Called")

        touchStartX = event.x
        touchStartY = event.y

        firstPointerId = event.getPointerId(event.actionIndex)
    }

    private fun onMotionEventActionPointerDown(event: MotionEvent) {
        Log.d(TAG, "onMotionEventActionPointerDown: Called")
        Log.d(TAG, "onMotionEventActionPointerDown: Action Index: ${event.actionIndex}")

        if(event.pointerCount <= 2) {
            secondPointerId = event.getPointerId(event.actionIndex)
            secondPointerStarted = true
            secondTouchStartX = event.getX(event.findPointerIndex(secondPointerId))
            secondTouchStartY = event.getY(event.findPointerIndex(secondPointerId))

        }
    }

    private fun onMotionEventActionMove(event: MotionEvent) {
        Log.d(TAG, "onMotionEventActionMove: Called")

        touchMoveX = event.x
        touchMoveY = event.y

        Log.d(TAG, "onMotionEventActionMove: Action Index: ${event.actionIndex}")
        Log.d(TAG, "onMotionEventActionMove: PointerId: ${event.getPointerId(event.actionIndex)}")
        Log.d(TAG, "onMotionEventActionMove: PointerCount: ${event.pointerCount}")
        Log.d(TAG, "onMotionEventActionMove: First Touch Point: ($touchMoveX,$touchMoveY)")
        Log.d(TAG, "onMotionEventActionMove: First PointerId: $firstPointerId")
        Log.d(TAG, "onMotionEventActionMove: Second PointerId: $secondPointerId")
        Log.d(TAG, "onMotionEventActionMove: Second Touch Point: " +
                "(${event.getX(event.findPointerIndex(secondPointerId))}," +
                "${event.getY(event.findPointerIndex(secondPointerId))})")

        if(!secondPointerStarted) {
            gestureEventListener.onDragGestureDetected(event)
        } else {
            // Check for Scale or Rotation Gesture
            checkForScaleGesture(event)
            checkForRotateGesture(event)
        }

    }

    private fun onMotionEventActionPointerUp(event: MotionEvent) {
        Log.d(TAG, "onMotionEventActionPointerUp: Called")
        secondPointerStarted = false
        secondPointerId = -1
    }

    private fun onMotionEventActionUp(event: MotionEvent) {
        Log.d(TAG, "onMotionEventActionUp: Called")
        resetAllPointers()
    }

    private fun checkForScaleGesture(event: MotionEvent) {
        val initialDistanceBetweenBothPointers = CommonMethods.getDistanceBetweenPoints(
            x1 = touchStartX,
            y1 = touchStartY,
            x2 = secondTouchStartX,
            y2 = secondTouchStartY
        )
        
        val currentDistanceBetweenBothPointers = CommonMethods.getDistanceBetweenPoints(
            x1 = event.x,
            y1 = event.y,
            x2 = event.getX(event.findPointerIndex(secondPointerId)),
            y2 = event.getY(event.findPointerIndex(secondPointerId))
        )

        Log.d(TAG, "checkForScaleGesture: Start First Point: ($touchStartX,$touchStartY)")
        Log.d(TAG, "checkForScaleGesture: Start Second Point: ($secondTouchStartX,$secondTouchStartY)")

        Log.d(TAG, "checkForScaleGesture: First Touch Point: (${event.x},${event.y})")
        Log.d(TAG, "checkForScaleGesture: Second Touch Point: " +
                "(${event.getX(event.findPointerIndex(secondPointerId))}," +
                "${event.getY(event.findPointerIndex(secondPointerId))})")
        Log.d(TAG, "checkForScaleGesture: Initial: $initialDistanceBetweenBothPointers")
        Log.d(TAG, "checkForScaleGesture: Current: $currentDistanceBetweenBothPointers")

        if(initialDistanceBetweenBothPointers > currentDistanceBetweenBothPointers) {
            Log.d(TAG, "checkForScaleGesture: SIZE DECREASED")
        } else if(initialDistanceBetweenBothPointers < currentDistanceBetweenBothPointers) {
            Log.d(TAG, "checkForScaleGesture: SIZE INCREASED")
        } else {
            Log.d(TAG, "checkForScaleGesture: SIZE EQUAL")
        }

        if(currentDistanceBetweenBothPointers == initialDistanceBetweenBothPointers) {
            return
        }

        val distanceChanged = currentDistanceBetweenBothPointers - initialDistanceBetweenBothPointers
        val scaleFactor = currentDistanceBetweenBothPointers / initialDistanceBetweenBothPointers
        gestureEventListener.onScaleGestureDetected(scaleFactor)

        Log.d(TAG, "checkForScaleGesture: Scale Factor: $scaleFactor")
    }

    private fun checkForRotateGesture(event: MotionEvent) {
        val initialTouchDiffY = secondTouchStartY - touchStartY
        val initialTouchDiffX = secondTouchStartX - touchStartX

        val initialAngle = atan2(initialTouchDiffY, initialTouchDiffX)
        val initialAngleInDegree = Math.toDegrees(initialAngle.toDouble())

        val currentTouchDiffY = event.getY(event.findPointerIndex(secondPointerId)) - event.y
        val currentTouchDiffX = event.getX(event.findPointerIndex(secondPointerId)) - event.x

        val currentAngle = atan2(currentTouchDiffY, currentTouchDiffX)
        val currentAngleInDegree = Math.toDegrees(currentAngle.toDouble())

        Log.d(TAG, "rotationTest: Initial Radian Angle: $initialAngle")
        Log.d(TAG, "rotationTest: Initial Degree Angle: $initialAngleInDegree")

        Log.d(TAG, "rotationTest: Current Radian Angle: $currentAngle")
        Log.d(TAG, "rotationTest: Current Degree Angle: $currentAngleInDegree")

        var diffAngle = Math.toDegrees((initialAngle - currentAngle).toDouble()) % 360

        if(diffAngle < -180.0) diffAngle += 360
        if(diffAngle > 180.0) diffAngle -= 360

        Log.d(TAG, "rotationTest: Diff Degree Angle: ${diffAngle}")

        gestureEventListener.onRotateGestureDetected(diffAngle.toFloat())
    }
    
    private fun resetAllPointers() {
        touchStartX = 0.0F
        touchStartY = 0.0F

        touchMoveX = 0.0F
        touchMoveY = 0.0F

        secondTouchStartX = 0.0F
        secondTouchStartY = 0.0F

        secondTouchMoveX = 0.0F
        secondTouchMoveY = 0.0F

        firstPointerId = -1
        secondPointerId = -1
        secondPointerStarted = false
    }

    interface GestureEventListener {
        fun onDragGestureDetected(event: MotionEvent)
        fun onScaleGestureDetected(scaleFactor: Float)
        fun onRotateGestureDetected(rotatedAngle: Float)
    }
}