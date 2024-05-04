package com.miguelangel.face2facev2

import android.graphics.Bitmap
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import kotlin.math.pow


class ClassificationModel(modelPath: String) {

    private val module = LiteModuleLoader.load(modelPath)

    private val classes = arrayOf("Happy", "Surprise", "Others")

    var predictedClass: String = ""

    var predictedProb: Double = 0.0

    val predictedProbs: DoubleArray = DoubleArray(3)

    fun predict(face: Mat) {
        val resized = Mat()
        Imgproc.resize(face, resized, Size(48.0, 48.0))

        // Conversion de Mat a Bitmap
        val bitmap = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(resized, bitmap)

        // Conversion de Bitmap a Tensor sin aplicar normalizacion
        val tensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
            arrayOf(0.0f, 0.0f, 0.0f).toFloatArray(), arrayOf(1.0f, 1.0f, 1.0f).toFloatArray())

        val outputTensor = module.forward(IValue.from(tensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray

        // Softmax
        var expSum = 0.0
        for (score in scores) {
            expSum += Math.E.pow(score.toDouble())
        }

        var highestProb = 0.0
        var highestProbIdx = 0
        for ((i, score) in scores.withIndex()) {
            val prob = Math.E.pow(score.toDouble()) / expSum
            predictedProbs[i] = prob
            if (prob > highestProb) {
                highestProb = prob
                highestProbIdx = i
            }
        }

        predictedClass = classes[highestProbIdx]
        predictedProb = highestProb
    }
}