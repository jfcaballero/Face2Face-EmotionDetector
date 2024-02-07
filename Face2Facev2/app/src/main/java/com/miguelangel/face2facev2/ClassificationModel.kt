package com.miguelangel.face2facev2

import android.content.Context
import android.graphics.Bitmap
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import kotlin.math.pow


class ClassificationModel(context: Context) {

    private val module = LiteModuleLoader.load(Utils.assetFilePath(context, "vgg19_fer.ptl"))

    private val classes = arrayOf("Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral")

    private val threshold = 0.25

    fun predict(face: Mat) {
        val resized = Mat()
        Imgproc.resize(face, resized, Size(48.0, 48.0))

        val resized4C = Mat()
        Imgproc.cvtColor(resized, resized4C, Imgproc.COLOR_GRAY2RGBA)

        // Conversion de Mat a Bitmap
        val bitmap = Bitmap.createBitmap(resized4C.cols(), resized4C.rows(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(resized4C, bitmap)

        // Conversion de Bitmap a Tensor sin aplicar normalizacion
        val tensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
            arrayOf(0.0f, 0.0f, 0.0f).toFloatArray(), arrayOf(1.0f, 1.0f, 1.0f).toFloatArray())

        val outputTensor = module.forward(IValue.from(tensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray

        println(getPredictedClass(scores))
    }

    private fun getPredictedClass(scores: FloatArray): String {
        var expSum = 0.0
        for (score in scores) {
            expSum += Math.E.pow(score.toDouble())
        }

        var highestProb = 0.0
        var highestProbIdx = 0
        for ((i, score) in scores.withIndex()) {
            val prob = Math.E.pow(score.toDouble()) / expSum
            if (prob > highestProb) {
                highestProb = prob
                highestProbIdx = i
            }
        }

        return classes[highestProbIdx]
    }
}