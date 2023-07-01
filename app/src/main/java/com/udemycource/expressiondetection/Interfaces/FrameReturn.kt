package com.udemycource.expressiondetection.Interfaces

import android.graphics.Bitmap
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.udemycource.expressiondetection.Common.FrameMetadata
import com.udemycource.expressiondetection.UIComponents.GraphicsOverlay

interface FrameReturn {
    fun onFrame(
        image: Bitmap?,
        face: FirebaseVisionFace?,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicsOverlay?
    )
}