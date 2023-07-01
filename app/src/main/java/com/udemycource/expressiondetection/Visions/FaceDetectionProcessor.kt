package com.udemycource.expressiondetection.Visions

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.udemycource.expressiondetection.Common.CameraImageGraphic
import com.udemycource.expressiondetection.Common.FrameMetadata
import com.udemycource.expressiondetection.Common.VisionProcessorBase
import com.udemycource.expressiondetection.Interfaces.FaceDetectStatus
import com.udemycource.expressiondetection.Interfaces.FrameReturn
import com.udemycource.expressiondetection.Models.RectModel
import com.udemycource.expressiondetection.R
import com.udemycource.expressiondetection.UIComponents.GraphicsOverlay
import java.io.IOException

class FaceDetectionProcessor(resources: Resources?) : VisionProcessorBase<List<FirebaseVisionFace?>?>(),
    FaceDetectStatus {
    var faceDetectStatus: FaceDetectStatus? = null
    private val detector: FirebaseVisionFaceDetector
    private val overlayBitmap: Bitmap
    var frameHandler: FrameReturn? = null

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage?): Task<List<FirebaseVisionFace?>?> {
        return detector.detectInImage(image!!)
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    override fun onFaceLocated(rectModel: RectModel?) {
        if (faceDetectStatus != null) faceDetectStatus!!.onFaceLocated(rectModel)
    }

    override fun onFaceNotLocated() {
        if (faceDetectStatus != null) faceDetectStatus!!.onFaceNotLocated()
    }

    companion object {
        private const val TAG = "FaceDetectionProcessor"
    }

    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        overlayBitmap = BitmapFactory.decodeResource(resources, R.drawable.clown_nose)
    }

    override fun onSuccess(originalCameraImage: Bitmap?, faces: List<FirebaseVisionFace?>?, frameMetadata: FrameMetadata, graphicOverlay: GraphicsOverlay) {
        graphicOverlay.clear()
        if (originalCameraImage != null) {
            val imageGraphic = CameraImageGraphic(graphicOverlay, originalCameraImage)
            graphicOverlay.add(imageGraphic)
        }
        for (i in faces!!.indices) {
            val face = faces[i]
            if (frameHandler != null) {
                frameHandler!!.onFrame(originalCameraImage, face, frameMetadata, graphicOverlay)
            }
            val cameraFacing = frameMetadata?.cameraFacing ?:  android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
            val faceGraphic = FaceGraphic(graphicOverlay, face!!, cameraFacing, overlayBitmap)
            faceGraphic.faceDetectStatus = this
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
    }
}