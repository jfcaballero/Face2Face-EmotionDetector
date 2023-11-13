package com.miguelangel.face2facev2

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class Utils {
    // Se utiliza companion object para que la funcion pueda llamarse como a una funcion static de Java
    companion object {
        // Establece la presentacion de la ventana con las barras del sistema ocultas y en modo apaisado
        fun setWindowPresentation(activity: Activity) {
            // Ocultar barras del sistema
            val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            // Poner aplicacion en modo apaisado
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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
    }
}