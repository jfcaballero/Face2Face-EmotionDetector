package com.miguelangel.face2facev2

import android.app.Activity
import android.content.pm.ActivityInfo
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
    }
}