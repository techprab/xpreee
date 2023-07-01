package com.udemycource.expressiondetection.Base

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*

object PublicMethods {
    private const val PERMISSION_REQUESTS = 1
    private fun getRequiredPermissions(mActivity: Activity): Array<String?> {
        return try {
            val info = mActivity.packageManager
                    .getPackageInfo(mActivity.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.size > 0) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    fun allPermissionsGranted(mActivity: Activity): Boolean {
        for (permission in getRequiredPermissions(mActivity)) {
            if (!isPermissionGranted(mActivity, permission)) {
                return false
            }
        }
        return true
    }

    fun getRuntimePermissions(mActivity: Activity) {
        val allNeededPermissions: MutableList<String?> = ArrayList()
        for (permission in getRequiredPermissions(mActivity)) {
            if (!isPermissionGranted(mActivity, permission)) {
                allNeededPermissions.add(permission)
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    mActivity, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String?): Boolean {
        return (ContextCompat.checkSelfPermission(context, permission!!)
                == PackageManager.PERMISSION_GRANTED)
    }


    fun saveToInternalStorage(bitmapImage: Bitmap, fileName: String?, mContext: Context): String {
        val directory = mContext.getDir("imageDir", Context.MODE_PRIVATE)
        val imgPath = File(directory, fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imgPath)
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    fun getBitmapByPath(path: String?, fileName: String?): Bitmap? {
        return try {
            val f = File(path, fileName)
            BitmapFactory.decodeStream(FileInputStream(f))
        } catch (e: FileNotFoundException) {
            null
        }
    }

}