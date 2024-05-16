package com.uco.face2facev2

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PredictionResultActivity : AppCompatActivity() {
    private var predThreshold: Double = 0.3

    private var predictedProbs: DoubleArray = DoubleArray(3)

    private var minDifference: Double = 1.0

    private var mute: Boolean = false

    private lateinit var seguirButton: ImageButton

    private lateinit var vozMediaPlayer: MediaPlayer

    private var voz: Int = 0

    private lateinit var sonidoMediaPlayer: MediaPlayer

    private var sonido: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction_result)
        Utils.hideSystemBars(this)

        val emotionId = intent?.extras?.getInt("emotionId") ?: 0
        predictedProbs = intent?.extras?.getDoubleArray("predictedProbs") ?: DoubleArray(3)
        mute = intent?.extras?.getBoolean("mute") ?: false

        val preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        predThreshold = preferences.getFloat("predThreshold", 0.3f).toDouble()

        for ((i, prob) in predictedProbs.withIndex()) {
            if (i != emotionId) {
                val difference = predictedProbs[emotionId] - prob
                if (difference < minDifference) {
                    minDifference = difference
                }
            }
        }

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

        if (minDifference >= predThreshold) {
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

                sonido = R.raw.aplausos
                voz = if(emotionId == 0) R.raw.muybienalegria else R.raw.muybiensorpresa
        }
        else {
            sonido = R.raw.errorohh

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

            val tryAgainText = findViewById<TextView>(R.id.tryagainText)
            tryAgainText.visibility = View.VISIBLE

            seguirButton.isClickable = false

            if (minDifference < 0) {
                voz = if(emotionId == 0) R.raw.recuerdamaria else R.raw.recuerdajavier
                libro.setImageResource(if(emotionId == 0) R.mipmap.libro_historia_maria2 else R.mipmap.libro_historia_javier2)
            }
            else {
                voz = if(emotionId == 0) R.raw.recuerdamaria2 else R.raw.recuerdajavier2
                libro.setImageResource(if(emotionId == 0) R.mipmap.libro_historia_maria1 else R.mipmap.libro_historia_javier1)
            }
        }

        sonidoMediaPlayer = MediaPlayer.create(applicationContext, sonido)
        sonidoMediaPlayer.setOnPreparedListener {
            sonidoMediaPlayer.start()
        }

        vozMediaPlayer = MediaPlayer.create(applicationContext, voz)
        vozMediaPlayer.setOnPreparedListener {
            vozMediaPlayer.start()
        }
    }

    override fun onPause() {
        super.onPause()

        if (sonidoMediaPlayer.isPlaying) {
            sonidoMediaPlayer.stop()
        }

        if (vozMediaPlayer.isPlaying) {
            vozMediaPlayer.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DetectorActivity.destroyPredBitmap()
        sonidoMediaPlayer.release()
        vozMediaPlayer.release()
    }
}