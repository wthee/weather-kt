package com.weather.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.ShareCompat
import java.io.File
import android.text.TextUtils
import android.R.layout
import android.graphics.Bitmap
import android.graphics.Canvas
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.NonNull


object ShareUtil{

    @Suppress("DEPRECATION")
    fun shareImg(uri: Uri, context: Context){
        val shareIntent = ShareCompat.IntentBuilder.from(ActivityUtil.instance.currentActivity!!)
            .setStream(uri)
            .setType("image/plain")
            .createChooserIntent()
            .apply {
                // https://android-developers.googleblog.com/2012/02/share-with-intents.html
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // If we're on Lollipop, we can open the intent as a document
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                } else {
                    // Else, we will use the old CLEAR_WHEN_TASK_RESET flag
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                }
            }

        context.startActivity(shareIntent)
    }

    @Suppress("DEPRECATION")
    fun shareText(text: String, context: Context){
        val shareIntent = ShareCompat.IntentBuilder.from(ActivityUtil.instance.currentActivity!!)
            .setText(text)
            .setType("text/plain")
            .createChooserIntent()
            .apply {
                // https://android-developers.googleblog.com/2012/02/share-with-intents.html
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // If we're on Lollipop, we can open the intent as a document
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                } else {
                    // Else, we will use the old CLEAR_WHEN_TASK_RESET flag
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                }
            }
        context.startActivity(shareIntent)
    }
}