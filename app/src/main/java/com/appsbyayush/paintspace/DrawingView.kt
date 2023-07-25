package com.appsbyayush.paintspace

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.MotionEventCompat
import com.appsbyayush.paintspace.customviews.DrawingView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class DrawingView(
    context: Context,
    attrs: AttributeSet
): View(context, attrs) {

    private var drawPaint: Paint = Paint()
    private var path: Path = Path()
    private var drawingPathList = mutableListOf<DrawingPath>()

    private var currentStrokeWidth: Float = DEFAULT_BRUSH_SIZE
    private var currentStrokeColor: Int = DEFAULT_COLOR
    private var currentMaskFilter: MaskFilter? = null

    private var startX: Float = 0F
    private var startY: Float = 0F

    private var bitmapRect: Rect? = null
    private var moveImage: Boolean = false

    private var scaleDetector: ScaleGestureDetector
    private var scaleFactor: Float = 1.0F

    private var imageHeight: Float = 200.0F
    private var imageWidth: Float = 200.0F

    private var pointers = mutableListOf<PointerInfo>()

    init {

        drawPaint.apply {
            isAntiAlias = true
            color = currentStrokeColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = currentStrokeWidth
            maskFilter = BlurMaskFilter(strokeWidth, BlurMaskFilter.Blur.SOLID)
        }

        

        scaleDetector = ScaleGestureDetector(context, object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                Log.d(TAG, "onScale: Called ${detector?.scaleFactor} ${detector?.focusX}")

                if(true) {
                    Log.d(TAG, "onScale: Increasing Image Size")
                    scaleFactor = detector?.scaleFactor!!
                    invalidate()
                }

                return super.onScale(detector)
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                super.onScaleEnd(detector)

//                imageHeight = bitmapRect?.height()?.toFloat()!!
//                imageWidth = bitmapRect?.width()?.toFloat()!!
            }
        })

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.save()

        addBackground(canvas)

//        if(canvas != null) {
//            val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_background)
//
//            if(drawable != null) {
//                Log.d(TAG, "onDraw: Image Drawable -- $drawable")
//                val imageLeft = bitmapRect?.left?.toFloat() ?: 150F
//                val imageTop = bitmapRect?.top?.toFloat() ?: 150F
//
//
//                val newImageHeight = imageHeight * scaleFactor
//                val newImageWidth = imageWidth * scaleFactor
//
//                Log.d(TAG, "onDraw: Called ImageHeight: $newImageHeight ImageWidth: $newImageHeight")
//
////                imageHeight = imageHeight * scaleFactor
////                imageWidth = imageWidth * scaleFactor
//
//                val imageBitmap = drawable.toBitmap(newImageHeight.toInt(),
//                    newImageWidth.toInt())
//
//                canvas.drawBitmap(imageBitmap, imageLeft, imageTop, drawPaint)
//
//                bitmapRect = Rect(imageLeft.toInt(), imageTop.toInt(), imageLeft.toInt() +
//                        imageBitmap.width,
//                    imageTop.toInt() + imageBitmap.height)
//            }
//        }

//        for(drawingPath in drawingPathList) {
//            drawPaint.apply {
//                strokeWidth = drawingPath.strokeWidth
//                color = drawingPath.strokeColor
//                maskFilter = drawingPath.maskFilter
//            }
//            canvas?.drawPath(drawingPath.path, drawPaint)
//        }

        Log.d(TAG, "onDraw: Called Pointers: $pointers")

//        paintTextOnCanvas(canvas)

        if(pointers.size > 1) {
            val firstPointer = pointers.first()
            val secondPointer = pointers[1]

            val path = Path()
            path.moveTo(firstPointer.x, firstPointer.y)
            path.lineTo(secondPointer.x, secondPointer.y)

            canvas?.drawPath(path, drawPaint)
        }

        canvas?.restore()
    }

//    private fun createMorphedImage(drawingCanvas: Canvas?) {
//        drawingCanvas?.let { canvas ->
//            Log.d(DrawingView.TAG, "createMorphedImage: Called")
//            canvas.drawColor(Color.GRAY)
//
//            val bgDrawable = AppCompatResources.getDrawable(context, R.drawable. ic_bg_test_1)
//            val overlayDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_bg_test_4)
//
//            val rect = Rect(150, 500, 650, 1500)
//
//            val bgBitmap = bgDrawable?.toBitmap()
//            val overlayBitmap = overlayDrawable?.toBitmap()
//
//            val combinedBitmap = Bitmap.createBitmap(bgBitmap?.width!!, bgBitmap.height,
//                Bitmap.Config.ARGB_8888)
//
////            val colorFilter = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
////                Log.d(TAG, "createMorphedImage: Version Q")
////                BlendModeColorFilter(Color.BLUE, BlendMode.EXCLUSION)
////            } else {
////                LightingColorFilter(5, 10)
////            }
//
//            val paint = Paint().apply {
//                isAntiAlias = true
//                setColorFilter(colorFilter)
////                xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
//            }
//
//            val combinedBitmapCanvas = Canvas(combinedBitmap)
//            val combinedBitmapRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
//
//            combinedBitmapCanvas.drawBitmap(bgBitmap!!, null, combinedBitmapRect, paint)
////            combinedBitmapCanvas.drawBitmap(overlayBitmap!!, null, combinedBitmapRect, paint)
//
//            canvas.drawBitmap(combinedBitmap, null, rect, null)
////            canvas.drawBitmap(combinedBitmap, 0F, 0F, paint)
//        }
//    }

    private fun addBackground(canvas: Canvas?) {
//        val drawable = AppCompatResources.getDrawable(context, R.drawable.canvas_back_2)

        val drawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.BLACK, 0xFF03DAC5.toInt()))

        if(drawable != null) {
            Log.d(TAG, "onDraw: Image Drawable -- $drawable")
            val imageLeft = bitmapRect?.left?.toFloat() ?: 0.0F
            val imageTop = bitmapRect?.top?.toFloat() ?: 0.0F


            val newImageHeight = imageHeight * scaleFactor
            val newImageWidth = imageWidth * scaleFactor

            Log.d(TAG, "onDraw: Called ImageHeight: $newImageHeight ImageWidth: $newImageWidth")

//                imageHeight = imageHeight * scaleFactor
//                imageWidth = imageWidth * scaleFactor

            val imageBitmap = drawable.toBitmap(width, height)

            canvas?.drawBitmap(imageBitmap, imageLeft, imageTop, drawPaint)

//            canvas?.drawARGB(150, 255, 150, 195)
            bitmapRect = Rect(imageLeft.toInt(), imageTop.toInt(), imageLeft.toInt() +
                    imageBitmap.width,
                imageTop.toInt() + imageBitmap.height)
        }
    }

    private fun paintTextOnCanvas(canvas: Canvas?) {
        val textPaint = Paint().apply {
            style = Paint.Style.FILL
            textSize = 100.0F
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        val textBackgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.teal_200)
        }

        val textToDraw = "Sample Text"
        val textBoundsRect = Rect()
        Log.d(TAG, "onDraw: RectText Before: $textBoundsRect")
        textPaint.getTextBounds(textToDraw, 0, textToDraw.length, textBoundsRect)

        val backWidth = textBoundsRect.width()
        val backHeight = textBoundsRect.height()

        textBoundsRect.apply {
            left = 230
            top = 230 - backHeight
            right = 270 + backWidth
            bottom = 270
        }

        Log.d(TAG, "onDraw: RectText After: $textBoundsRect")

        canvas?.drawRoundRect(RectF(textBoundsRect), 25.0F, 25.0F, textBackgroundPaint)
        canvas?.drawText(textToDraw, 250.0F, 250.0F, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleDetector.onTouchEvent(event)

//        Log.d(TAG, "on TouchEvent: Called Type = ${event?.action} X = ${event?.x} Y = ${event?.y}")

        val touchX = event?.x
        val touchY = event?.y

        if(touchX == null || touchY == null) {
            return false
        }

        if(pointers.size == 2) {
            checkRotation()
        }

//        Log.d(TAG, "onTouchEvent Called Rotation: ${rotation(event)}")

        Log.d(TAG, "onTouchEvent: ${actionToString(event.actionMasked)} " +
                "X = ${event.getX(event.actionIndex)} Y = ${event.getY(event.actionIndex)}")

        val pointerId = event.getPointerId(event.actionIndex)

//        when(event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                pointers.add(PointerInfo(pointerId, event.getX(event.actionIndex),
//                    event.getY(event.actionIndex)))
//            }
//
//            MotionEvent.ACTION_MOVE -> {
////                Log.d(TAG, "onTouchEvent: PointerId: ${pointerId}")
//                pointers.forEach {
//                    val index = event.findPointerIndex(it.pointerId)
//
//                    it.x = event.getX(index)
//                    it.y = event.getY(index)
//                }
//            }
//
//            MotionEvent.ACTION_UP -> {
//                pointers = pointers.filter { it.pointerId != pointerId }.toMutableList()
//            }
//
//            MotionEvent.ACTION_POINTER_DOWN -> {
//                Log.d(TAG, "onTouchEvent: New Pointer Down ${event.pointerCount}")
//                if(event.pointerCount <= 2) {
//                    pointers.add(PointerInfo(pointerId, event.getX(event.actionIndex),
//                        event.getY(event.actionIndex)))
//                }
//            }
//            MotionEvent.ACTION_POINTER_UP -> {
//                pointers = pointers.filter { it.pointerId != pointerId }.toMutableList()
//            }
//
//            MotionEvent.ACTION_OUTSIDE -> {
//
//            }
//            MotionEvent.ACTION_CANCEL -> {
//
//            }
//        }

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                if(bitmapRect?.contains(touchX.toInt(), touchY.toInt()) == true) {
                    Log.d(TAG, "onTouchEvent: Image Clicked")
                    moveImage = true
                } else {
                    Log.d(TAG, "onTouchEvent: Image NOT Clicked")
                    moveImage = false
                }

//                touchStart(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                if(moveImage) {
                    bitmapRect?.left = touchX.toInt() - (bitmapRect?.width()!! / 2)
                    bitmapRect?.top = touchY.toInt() - (bitmapRect?.height()!! / 2)
                }
//                touchMove(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                moveImage = false

                imageHeight = bitmapRect?.height()?.toFloat()!!
                imageWidth = bitmapRect?.width()?.toFloat()!!

                scaleFactor = 1.0F

                Log.d(TAG, "onTouchEvent: Called ImageHeight: $imageHeight ImageWidth: $imageHeight")
                Log.d(TAG, "onTouchEvent: Called Stopping Touch Event...")
//                touchUp(touchX, touchY)
            }
        }

        invalidate()
        return true
    }

    private fun checkRotation() {

    }

    fun actionToString(action: Int): String {
        return when (action) {
            MotionEvent.ACTION_DOWN -> "Down"
            MotionEvent.ACTION_MOVE -> "Move"
            MotionEvent.ACTION_POINTER_DOWN -> "Pointer Down"
            MotionEvent.ACTION_UP -> "Up"
            MotionEvent.ACTION_POINTER_UP -> "Pointer Up"
            MotionEvent.ACTION_OUTSIDE -> "Outside"
            MotionEvent.ACTION_CANCEL -> "Cancel"
            else -> ""
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)

        Log.d(
            "Rotation ~~~~~~~~~~~~~~~~~",
            delta_x.toString() + " ## " + delta_y + " ## " + radians + " ## "
                    + Math.toDegrees(radians))
            
        return Math.toDegrees(radians).toFloat()
    }

    private fun touchStart(x: Float, y: Float) {
        path = Path()
        drawingPathList.add(getCurrentDrawingPath())

        path.reset()
        path.moveTo(x, y)

        startX = x
        startY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - startX)
        val dy = abs(y - startY)

        if(dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(startX, startY, (x + startX) / 2, (y + startY) / 2)

            startX = x
            startY = y
        }
    }

    private fun touchUp(x: Float, y: Float) {
        path.lineTo(x, y)
    }

    private fun getCurrentDrawingPath() = DrawingPath(
            path,
            currentStrokeColor,
            currentStrokeWidth,
            currentMaskFilter
    )

    fun setBrushSize(width: Float) {
        currentStrokeWidth = width
    }

    fun setBrushColor(color: Int) {
        currentStrokeColor = color
    }

    fun setBlurEffect(blurEffect: BlurMaskFilter.Blur?) {
        if(blurEffect == null) {
            currentMaskFilter = null
            return
        }

        currentMaskFilter = BlurMaskFilter(currentStrokeWidth, blurEffect)
    }

    fun undo() {
        drawingPathList.removeLast()
        invalidate()
    }

    fun clearView() {
        drawingPathList.clear()

        path.reset()
        invalidate()
    }

    fun save(): Pair<String, Boolean> {

        /*COPYING PAINTING ON BITMAP CANVAS*/
        val drawBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val drawCanvas = Canvas(drawBitmap)
        drawCanvas.drawColor(DEFAULT_BG_COLOR)

        for(drawingPath in drawingPathList) {
            drawPaint.apply {
                strokeWidth = drawingPath.strokeWidth
                color = drawingPath.strokeColor
                maskFilter = drawingPath.maskFilter
            }
            drawCanvas.drawPath(drawingPath.path, drawPaint)
        }

        /*COMPRESSING BITMAP AND SAVING AS PNG IMAGE*/
        val timeStamp = SimpleDateFormat(MainActivity.FILE_NAME_FORMAT, Locale.UK).format(System.currentTimeMillis())

        val filePath = context.getExternalFilesDir(MainActivity.PAINTINGS_DIR)?.path + "/Paint$timeStamp.png"
        val file = File(filePath)

        val isFileSaved = try {
            val outputStream = FileOutputStream(file)
            drawBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            drawBitmap.recycle()
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

        return Pair(filePath, isFileSaved)
    }

    companion object {
        private const val TAG = "DrawingViewyy"

        private const val DEFAULT_BRUSH_SIZE = 20F
        private const val DEFAULT_COLOR: Int = Color.GRAY
        private const val DEFAULT_BG_COLOR: Int = Color.WHITE
        private const val TOUCH_TOLERANCE = 4f
    }
}