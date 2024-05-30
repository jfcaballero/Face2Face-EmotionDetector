package com.uco.face2facev2

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.FaceDetectorYN
import java.lang.Double.max
import java.lang.Double.min
import java.util.*
import kotlin.math.round

class DetectorActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var cameraView: JavaCameraView

    private lateinit var frame: Mat

    private lateinit var faces: Mat

    private lateinit var detectorInput: Mat

    private lateinit var volverButton: ImageButton

    private lateinit var vozMediaPlayer: MediaPlayer

    private var useCameraButton = true

    private var cameraButtonPressed: Boolean = false

    private var cameraButton: ImageButton? = null

    private var cameraButtonBackground: ImageView? = null

    private var mute: Boolean = false

    private var emotionId: Int = 0

    private lateinit var faceIcon: ImageView

    private val classificationModel = ClassificationModel()

    private var timerDuration: Int = 5

    private var timerStart: Long = 0L

    private var countdown: TextView? = null

    private var predicting: Boolean = false

    /**
     * Companion object para almacenar objetos y funciones static
     */
    companion object {

        private var detector: FaceDetectorYN? = null    // Evita consumo innecesario de memoria creando el detector muchas veces

        private var predBitmap: Bitmap? = null

        private var cvInitialized: Boolean = false

        init {
            cvInitialized = OpenCVLoader.initDebug()
        }

        fun getPredBitmap(): Bitmap? {
            return predBitmap
        }

        fun destroyPredBitmap() {
            predBitmap?.recycle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detector)
        Utils.hideSystemBars(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mute = intent?.extras?.getBoolean("mute") ?: false

        if(!mute)
            Utils.playSound(applicationContext, R.raw.violin_aparecer)

        volverButton = findViewById(R.id.buttonVolver)

        volverButton.setOnClickListener(View.OnClickListener {
            if(!mute) {
                Utils.playSound(applicationContext, R.raw.pulsar_boton)
                Utils.playSound(applicationContext, R.raw.violin_desaparecer)
            }

            val context = volverButton.context
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("mute", mute)
            context.startActivity(intent)
        })

        val preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        useCameraButton = preferences.getBoolean("useCameraButton", true)
        timerDuration = preferences.getInt("timer", 5)

        if (useCameraButton) {
            cameraButton = findViewById(R.id.camara)
            cameraButton?.isClickable = false
            cameraButton?.visibility = View.VISIBLE

            cameraButton?.setOnClickListener {
                cameraButtonPressed = true
            }
            cameraButtonBackground = findViewById(R.id.botonblanco)
            cameraButtonBackground?.visibility = View.VISIBLE

            findViewById<ImageView>(R.id.carton_pulsar).visibility = View.VISIBLE
            findViewById<TextView>(R.id.texto_pulsar).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.camara_pulsar).visibility = View.VISIBLE
        }
        else {
            countdown = findViewById(R.id.countdown)
            countdown?.visibility = View.VISIBLE
        }

        emotionId = intent?.extras?.getInt("emotionId") ?: 0

        faceIcon = findViewById(R.id.cara)
        faceIcon.setImageResource(if(emotionId == 0) R.mipmap.cara_alegria else R.mipmap.cara_asombro)

        vozMediaPlayer = MediaPlayer.create(applicationContext, if(emotionId == 0) R.raw.poneralegria else R.raw.ponersorpresa)
        vozMediaPlayer.setOnPreparedListener {
            vozMediaPlayer.start()
        }

        cameraView = findViewById(R.id.camera_view)
        cameraView = findViewById<JavaCameraView>(R.id.camera_view).apply {
            visibility = CameraBridgeViewBase.VISIBLE
            setCameraPermissionGranted()
            setCvCameraViewListener(this@DetectorActivity)
        }

        if(cvInitialized) {
            if (detector == null) {
                detector = FaceDetectorYN.create(Utils.assetFilePath(applicationContext, getString(R.string.detection_model)),
                    "", Size(320.0, 320.0)
                )
            }
            classificationModel.load(Utils.assetFilePath(applicationContext, getString(R.string.classification_model)))

            cameraView.enableView()
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.enableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
        vozMediaPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()

        if (vozMediaPlayer.isPlaying) {
            vozMediaPlayer.stop()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        frame = Mat()
        faces = Mat()
        detectorInput = Mat()
    }

    override fun onCameraViewStopped() {
        frame.release()
        faces.release()
        detectorInput.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        frame = inputFrame!!.rgba()
        val framePortrait = Mat()
        Core.transpose(frame, framePortrait)
        Core.rotate(framePortrait, framePortrait, Core.ROTATE_180)
        val frameGray = Mat()
        Imgproc.cvtColor(framePortrait, frameGray, Imgproc.COLOR_BGRA2GRAY)

        Imgproc.cvtColor(framePortrait, detectorInput, Imgproc.COLOR_BGRA2RGB)

        detector!!.inputSize = detectorInput.size()
        detector!!.detect(detectorInput, faces)

        if(faces.rows() > 0) {
            if (useCameraButton) {
                runOnUiThread {
                    cameraButton?.isClickable = true
                }
            }
            else {
                if (timerStart == 0L) {
                    timerStart = System.currentTimeMillis()
                }
                else {
                    runOnUiThread {
                        val updatedCountdown = (timerDuration - round((System.currentTimeMillis() - timerStart) / 1000.0).toInt())
                        countdown?.text = if (updatedCountdown > 0) updatedCountdown.toString() else "0"
                    }
                }
            }

            for(i in 0 until faces.rows()) {
                val rectX = faces.get(i, 0)[0]
                val rectY = faces.get(i, 1)[0]
                val rectWidth = faces.get(i, 2)[0]
                val rectHeight = faces.get(i, 3)[0]

                val rectTopCorner = Point(max(0.0, rectX), max(0.0, rectY))
                val rectBottomCorner = Point(min(framePortrait.width().toDouble(), rectX + rectWidth),
                    min(framePortrait.height().toDouble(), rectY + rectHeight))

                val faceMat = frameGray.submat(rectTopCorner.y.toInt(), rectBottomCorner.y.toInt(),
                    rectTopCorner.x.toInt(), rectBottomCorner.x.toInt())

                val predCondition = (useCameraButton && cameraButtonPressed) ||
                        (!useCameraButton && timerStart != 0L && System.currentTimeMillis() - timerStart >= timerDuration * 1000)
                if (predCondition && !predicting) {
                    predicting = true
                    Utils.playSound(applicationContext, R.raw.disparocamara)
                    classificationModel.predict(faceMat)

                    predBitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
                    org.opencv.android.Utils.matToBitmap(frame, predBitmap)

                    runOnUiThread {
                        val context = this@DetectorActivity
                        val intent = Intent(context, PredictionResultActivity::class.java)
                        intent.putExtra("mute", mute)
                        intent.putExtra("emotionId", emotionId)
                        intent.putExtra("predictedProbs", classificationModel.predictedProbs)

                        context.startActivity(intent)
                    }
                }

                // Rotacion de la esquina del rectangulo 180 grados sobre el centro de la imagen
                // para deshacer la rotacion hecha al principio
                val center = Point(framePortrait.size().width / 2, framePortrait.size().height / 2)
                val rectXRot = -(rectX - center.x) + center.x
                val rectYRot = -(rectY - center.y) + center.y

                Imgproc.rectangle(
                    frame,
                    Point(rectYRot, rectXRot),
                    Point(rectYRot - rectHeight, rectXRot - rectWidth),
                    Scalar(0.0, 255.0, 0.0),
                    4
                )

                faceMat.release()
            }
        }
        else {
            if (useCameraButton) {
                runOnUiThread {
                    cameraButton?.isClickable = false
                }
            }
            else {
                if (timerStart > 0L) {
                    val newTimerDuration = timerDuration - round((System.currentTimeMillis() - timerStart) / 1000.0).toInt()
                    timerDuration = newTimerDuration
                    timerStart = 0L
                }
                runOnUiThread {
                    countdown?.text = timerDuration.toString()
                }
            }
        }

        framePortrait.release()
        frameGray.release()
        return frame
    }
}