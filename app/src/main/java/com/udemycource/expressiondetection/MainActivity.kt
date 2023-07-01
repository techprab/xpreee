package com.udemycource.expressiondetection

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.hsalf.smilerating.BaseRating
import com.hsalf.smilerating.SmileRating
import com.udemycource.expressiondetection.Base.BaseActivity
import com.udemycource.expressiondetection.Base.Cons
import com.udemycource.expressiondetection.Base.PublicMethods
import com.udemycource.expressiondetection.Base.Screenshot
import com.udemycource.expressiondetection.Common.CameraSource
import com.udemycource.expressiondetection.Common.FrameMetadata
import com.udemycource.expressiondetection.Interfaces.FaceDetectStatus
import com.udemycource.expressiondetection.Interfaces.FrameReturn
import com.udemycource.expressiondetection.Models.RectModel
import com.udemycource.expressiondetection.UIComponents.CameraPreview
import com.udemycource.expressiondetection.UIComponents.GraphicsOverlay
import com.udemycource.expressiondetection.Visions.FaceDetectionProcessor
import com.udemycource.expressiondetection.photo_viewer.PhotoViewerActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException

class MainActivity : BaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback, FrameReturn,
    FaceDetectStatus {

    var originalImage: Bitmap? = null
    private var cameraSource: CameraSource? = null
    private var preview: CameraPreview? = null
    private var graphicOverlay: GraphicsOverlay? = null
    private var faceFrame: ImageView? = null
    private var test: ImageView? = null
    private var takePhoto: Button? = null
    private var smile_rating: SmileRating? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test = findViewById(R.id.test)
        preview = findViewById(R.id.preview)
        takePhoto = findViewById(R.id.takePhoto)
        graphicOverlay = findViewById(R.id.overlay)
        smile_rating = findViewById(R.id.smile_rating)

        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource()
        } else {
            PublicMethods.getRuntimePermissions(this)
        }

        takePhoto!!.setOnClickListener(View.OnClickListener { v: View? -> takePhoto() })
    }



    @Subscribe
    fun OnAddSelected(add : String?) {
        if (add == "Return") {
            takePhoto!!.visibility = View.VISIBLE
            test!!.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource!!.release()
        }
        EventBus.getDefault().unregister(this)
    }

    private fun takePhoto() {
        takePhoto!!.visibility = View.GONE
        test!!.visibility = View.GONE

        val b = Screenshot.takescreenshotOfRootView(test!!)
        test!!.setImageBitmap(b)

        val path = PublicMethods.saveToInternalStorage(b!!, Cons.IMG_FILE, mActivity)

        startActivity(
            Intent(mActivity, PhotoViewerActivity::class.java)
            .putExtra(Cons.IMG_EXTRA_KEY, path))
    }

    private fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(this, graphicOverlay!!)
        }

        try {
            val processor = FaceDetectionProcessor(resources)
            processor.frameHandler = this
            processor.faceDetectStatus = this
            cameraSource!!.setMachineLearningFrameProcessor(processor)
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: $FACE_DETECTION", e)
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG)
                .show()
        }
    }

    public override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        preview!!.stop()
    }

    companion object {
        private const val FACE_DETECTION = "Face Detection"
        private const val TAG = "MLKitTAG"
    }

    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }


    override fun onFrame(image: Bitmap?, face: FirebaseVisionFace?, frameMetadata: FrameMetadata?, graphicOverlay: GraphicsOverlay?) {
        originalImage = image
        if (face!!.leftEyeOpenProbability < 0.4) {
            findViewById<View>(R.id.rightEyeStatus).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.rightEyeStatus).visibility = View.INVISIBLE
        }
        if (face.rightEyeOpenProbability < 0.4) {
            findViewById<View>(R.id.leftEyeStatus).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.leftEyeStatus).visibility = View.INVISIBLE
        }
        var smile = 0
        if (face.smilingProbability > .8) {
            smile = BaseRating.GREAT
        } else if (face.smilingProbability <= .8 && face.smilingProbability > .6) {
            smile = BaseRating.GOOD
        } else if (face.smilingProbability <= .6 && face.smilingProbability > .4) {
            smile = BaseRating.OKAY
        } else if (face.smilingProbability <= .4 && face.smilingProbability > .2) {
            smile = BaseRating.BAD
        }
        smile_rating!!.setSelectedSmile(smile, true)
    }


    override fun onFaceLocated(rectModel: RectModel?) {

    }

    override fun onFaceNotLocated() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}