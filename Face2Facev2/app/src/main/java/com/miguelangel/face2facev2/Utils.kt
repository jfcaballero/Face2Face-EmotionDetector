package com.miguelangel.face2facev2

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Utils {
    // Se utiliza companion object para que la funcion pueda llamarse como a una funcion static de Java
    companion object {
        // Establece la presentacion de la ventana con las barras del sistema ocultas y en modo apaisado
        fun hideSystemBars(activity: Activity) {
            val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        fun requestPermission(activity: Activity) {
            if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA), 100)
            }
        }

        fun playSound(context: Context, soundId: Int) {
            val sound = MediaPlayer.create(context, soundId)

            sound.seekTo(0)
            sound.start()
            sound.setOnCompletionListener {
                sound.release()
            }
        }

        fun assetFilePath(context: Context, assetName: String): String {
            val file = File(context.filesDir, assetName)

            if (file.exists() && file.length() > 0) {
                return file.absolutePath;
            }

            try {
                val inputStream = context.assets.open(assetName)
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(4096)
                var readBytes = inputStream.read(buffer)

                while(readBytes != -1) {
                    outputStream.write(buffer, 0, readBytes)
                    readBytes = inputStream.read(buffer)
                }

                inputStream.close()
                outputStream.close()
                return file.absolutePath

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return ""
        }
    }
}