package com.miguelangel.face2facev2

import android.graphics.Bitmap
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import kotlin.math.pow


class ClassificationModel {

    val predictedProbs: DoubleArray = DoubleArray(3)

    companion object {
        private var module: Module? = null
    }

    fun load(modelPath: String) {
        if (module == null) {
            module = LiteModuleLoader.load(modelPath)
        }
    }
    fun predict(face: Mat) {
        if (module == null) {
            throw IllegalStateException("La red neuronal no ha sido cargada mediante el metodo create")
        }

        val resized = Mat()
        Imgproc.resize(face, resized, Size(48.0, 48.0))

        // Conversion de Mat a Bitmap
        val bitmap = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(resized, bitmap)

        // Conversion de Bitmap a Tensor sin aplicar normalizacion
        val tensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
            arrayOf(0.0f, 0.0f, 0.0f).toFloatArray(), arrayOf(1.0f, 1.0f, 1.0f).toFloatArray())

        val outputTensor = module!!.forward(IValue.from(tensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray

        // Softmax
        var expSum = 0.0
        for (score in scores) {
            expSum += Math.E.pow(score.toDouble())
        }

        for ((i, score) in scores.withIndex()) {
            predictedProbs[i] = Math.E.pow(score.toDouble()) / expSum
        }

        resized.release()
        bitmap.recycle()
    }
}