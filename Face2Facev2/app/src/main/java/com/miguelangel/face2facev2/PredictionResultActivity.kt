package com.miguelangel.face2facev2

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PredictionResultActivity : AppCompatActivity() {
    private val predThreshold: Double = 0.25

    private var isCorrect: Boolean = false

    private var predictedProb: Double = 0.0

    private var mute: Boolean = false

    private lateinit var seguirButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction_result)
        Utils.setWindowPresentation(this)

        val emotionId = intent?.extras?.getInt("emotionId") ?: 0
        isCorrect = intent?.extras?.getBoolean("correct") ?: false
        predictedProb = intent?.extras?.getDouble("predictedProb") ?: 0.0
        mute = intent?.extras?.getBoolean("mute") ?: false

        seguirButton = findViewById(R.id.buttonSeguir)

        seguirButton.setOnClickListener(View.OnClickListener {
            if(!mute) {
                Utils.playSound(applicationContext, R.raw.pulsar_boton)
            }

            val context = seguirButton.context
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("mute", mute)
            context.startActivity(intent)
        })

        if (isCorrect && predictedProb > predThreshold) {
                val bitmap = DetectorActivity.getPredBitmap()
                val predictedImage = findViewById<ImageView>(R.id.image)
                predictedImage.setImageBitmap(bitmap)
                predictedImage.visibility = View.VISIBLE

                val marco = findViewById<ImageView>(R.id.marco)
                marco.visibility = View.VISIBLE

                val muelle = findViewById<ImageView>(R.id.muelle)
                muelle.visibility = View.VISIBLE

                val cartel = findViewById<ImageView>(R.id.cartelMuyBien)
                cartel.visibility = View.VISIBLE

                val bombilla = findViewById<ImageView>(R.id.bombilla)
                bombilla.visibility = View.VISIBLE

                Utils.playSound(this, R.raw.aplausos)
                Utils.playSound(this, if(emotionId == 0) R.raw.muybienalegria else R.raw.muybiensorpresa)
        }
        else {
            Utils.playSound(this, R.raw.errorohh)

            val libro = findViewById<ImageView>(R.id.libro)
            libro.visibility = View.VISIBLE

            val tryAgain = findViewById<ImageButton>(R.id.tryagain)
            tryAgain.visibility = View.VISIBLE
            tryAgain.isClickable = true
            tryAgain.setOnClickListener {
                val context = tryAgain.context
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("mute", mute)
                context.startActivity(intent)
            }

            seguirButton.isClickable = false

            if (!isCorrect) {
                Utils.playSound(this, if(emotionId == 0) R.raw.recuerdamaria2 else R.raw.recuerdajavier2)
                libro.setImageResource(if(emotionId == 0) R.mipmap.libro_historia_maria2 else R.mipmap.libro_historia_javier2)
            }
            else {
                Utils.playSound(this, if(emotionId == 0) R.raw.recuerdamaria else R.raw.recuerdajavier)
                libro.setImageResource(if(emotionId == 0) R.mipmap.libro_historia_maria1 else R.mipmap.libro_historia_javier1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DetectorActivity.destroyPredBitmap()
    }
}