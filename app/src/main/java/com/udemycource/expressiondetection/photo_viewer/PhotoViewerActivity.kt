package com.udemycource.expressiondetection.photo_viewer

import android.os.Bundle
import android.widget.ImageView
import com.udemycource.expressiondetection.Base.BaseActivity
import com.udemycource.expressiondetection.Base.Cons
import com.udemycource.expressiondetection.Base.PublicMethods
import com.udemycource.expressiondetection.R
import org.greenrobot.eventbus.EventBus

class PhotoViewerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        EventBus.getDefault().post("Return")

        if (intent.hasExtra(Cons.IMG_EXTRA_KEY)) {
            val imageView = findViewById<ImageView>(R.id.image)
            val imagePath = intent.getStringExtra(Cons.IMG_EXTRA_KEY)
            imageView.setImageBitmap(PublicMethods.getBitmapByPath(imagePath, Cons.IMG_FILE))
        }
    }

}