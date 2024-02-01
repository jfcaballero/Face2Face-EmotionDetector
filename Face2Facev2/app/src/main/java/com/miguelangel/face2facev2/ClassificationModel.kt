package com.miguelangel.face2facev2

import android.content.Context
import org.opencv.core.Mat
import org.pytorch.Module

class ClassificationModel(currentContext: Context) {
    private val context = currentContext
    private val module = Module.load(Utils.assetFilePath(context, "vgg19_fer.pt"))

    fun predict(face: Mat) {

    }
}