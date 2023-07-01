package com.udemycource.expressiondetection.Interfaces

import com.udemycource.expressiondetection.Models.RectModel

interface FaceDetectStatus {
    fun onFaceLocated(rectModel: RectModel?)
    fun onFaceNotLocated()
}