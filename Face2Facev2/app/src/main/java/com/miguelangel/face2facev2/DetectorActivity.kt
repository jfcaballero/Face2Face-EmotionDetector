package com.miguelangel.face2facev2

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.FaceDetectorYN
import java.io.ByteArrayOutputStream
import java.lang.Double.min
import java.util.*

class DetectorActivity : CameraActivity() {
    private lateinit var cameraView: JavaCameraView

    private lateinit var frame: Mat

    private lateinit var detector: FaceDetectorYN

    private lateinit var faces: Mat

    private lateinit var detectorInput: Mat

    private lateinit var volverButton: ImageButton

    private lateinit var cameraButton: ImageButton

    private lateinit var cameraButtonBackground: ImageView

    private var mute: Boolean = false

    private var emotion: String = ""

    private lateinit var faceIcon: ImageView

    private lateinit var classificationModel: ClassificationModel

    private var predictCurrentFrame: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detector)
        Utils.setWindowPresentation(this)

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

        cameraButton = findViewById(R.id.camara)
        cameraButton.setOnClickListener {
            Utils.playSound(applicationContext, R.raw.disparocamara)
            predictCurrentFrame = true
        }
        cameraButtonBackground = findViewById(R.id.botonblanco)

        val emotionId = intent?.extras?.getInt("emotionId") ?: 0
        emotion = if(emotionId == 0) "Happy" else "Surprise"

        faceIcon = findViewById(R.id.cara)
        faceIcon.setImageResource(if(emotionId == 0) R.mipmap.cara_alegria else R.mipmap.cara_asombro)

        Utils.playSound(this, if(emotionId == 0) R.raw.poneralegria else R.raw.ponersorpresa)

        cameraView = findViewById(R.id.camera_view)
        cameraView.setCvCameraViewListener(object: CameraBridgeViewBase.CvCameraViewListener2 {
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
                val frameGray = inputFrame.gray()

                Imgproc.cvtColor(frame, detectorInput, Imgproc.COLOR_BGRA2RGB)

                detector.inputSize = detectorInput.size()
                detector.detect(detectorInput, faces)

                if(faces.rows() > 0) {
                    runOnUiThread {
                        cameraButton.isClickable = true
                        cameraButton.visibility = View.VISIBLE
                        cameraButtonBackground.visibility = View.VISIBLE
                    }

                    for(i in 0 until faces.rows()) {
                        val rectX = faces.get(i, 0)[0]
                        val rectY = faces.get(i, 1)[0]
                        val rectWidth = faces.get(i, 2)[0]
                        val rectHeight = faces.get(i, 3)[0]

                        val rectBottomCorner = Point(min(frame.width().toDouble(), rectX + rectWidth),
                            min(frame.height().toDouble(), rectY + rectHeight))

                        val faceMat = frameGray.submat(rectY.toInt(), rectBottomCorner.y.toInt(),
                            rectX.toInt(), rectBottomCorner.x.toInt())

                        if (predictCurrentFrame) {
                            classificationModel.predict(faceMat)

                            val bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
                            org.opencv.android.Utils.matToBitmap(frame, bitmap)
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                            runOnUiThread {
                                val context = this@DetectorActivity
                                val intent = Intent(context, PredictionResultActivity::class.java)
                                intent.putExtra("mute", mute)
                                intent.putExtra("emotionId", emotionId)
                                intent.putExtra("correct", classificationModel.predictedClass == emotion)
                                intent.putExtra("predictedProb", classificationModel.predictedProb)
                                intent.putExtra("image", stream.toByteArray())

                                context.startActivity(intent)
                            }

                        }

                        Imgproc.rectangle(frame, Point(rectX, rectY), rectBottomCorner,
                            Scalar(0.0, 255.0, 0.0), 4)

                        faceMat.release()
                    }
                }
                else {
                    runOnUiThread {
                        cameraButton.isClickable = false
                        cameraButton.visibility = View.INVISIBLE
                        cameraButtonBackground.visibility = View.INVISIBLE
                    }
                }

                frameGray.release()
                return frame
            }
        })

        if(OpenCVLoader.initDebug()) {
            cameraView.enableView()
            detector = FaceDetectorYN.create(Utils.assetFilePath(applicationContext, "face_detection_yunet_2023mar.onnx"),
                "", Size(320.0, 320.0)
            )
            classificationModel = ClassificationModel(applicationContext)
        }
    }

    override fun getCameraViewList(): MutableList<out CameraBridgeViewBase> {
        return Collections.singletonList(cameraView)
    }

    override fun onResume() {
        super.onResume()
        cameraView.enableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }
}