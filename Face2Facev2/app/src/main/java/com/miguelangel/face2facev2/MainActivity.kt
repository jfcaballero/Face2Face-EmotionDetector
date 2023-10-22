package com.miguelangel.face2facev2

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    private var backgroundMusic: MediaPlayer? = null

    private var buttonSound: MediaPlayer? = null

    private var mute: Boolean = false

    private lateinit var sonidoButton: ImageButton

    private lateinit var creditos: ImageView

    private lateinit var historiaSorpresa: ImageButton

    private lateinit var historiaAlegria: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Utils.setWindowPresentation(this)
        playBackgroundMusic()

        // Se activa un listener para poder poner la aplicacion en mute
        sonidoButton = findViewById(R.id.sonido)

        sonidoButton.setOnClickListener(View.OnClickListener {
            if (!mute) {
                sonidoButton.setImageResource(R.drawable.vol_mute)
                mute = true

                if (backgroundMusic?.isPlaying == true) {
                    backgroundMusic?.pause()
                }
            } else {
                sonidoButton.setImageResource(R.drawable.vol_up)
                mute = false

                if (backgroundMusic?.isPlaying == false) {
                    backgroundMusic?.start()
                }
            }
        })

        historiaAlegria = findViewById(R.id.historiaAlegria)
        setStoryListener(historiaAlegria, 0)
        historiaSorpresa = findViewById(R.id.historiaSorpresa)
        setStoryListener(historiaSorpresa, 1)

        creditos = findViewById(R.id.creditos)
        val creditosButton = findViewById<ImageButton>(R.id.buttonCreditos)

        creditosButton.setOnClickListener(View.OnClickListener {
            if (!mute)
                playButtonSound()

            if (creditos.visibility != View.VISIBLE)
                creditos.visibility = View.VISIBLE
            else
                creditos.visibility = View.GONE
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        // Se libera la memoria del MediaPlayer
        backgroundMusic?.release()
        buttonSound?.release()
    }

    override fun onPause() {
        super.onPause()
        // Si esta sonando la musica, se pausa
        if (backgroundMusic?.isPlaying == true) {
            backgroundMusic?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        // Se reaunda la musica por donde se quedo en onPause
        backgroundMusic?.start()
    }

    private fun setStoryListener(button: ImageButton, id: Int) {
        button.setOnClickListener(View.OnClickListener {
            if(!mute)
                playButtonSound()
            val context = button.context
            val intent = Intent(context, StoryActivity::class.java)
            intent.putExtra("storyId", id)
            intent.putExtra("mute", mute)
            context.startActivity(intent)
        })
    }

    private fun playBackgroundMusic() {
        backgroundMusic = MediaPlayer.create(applicationContext, R.raw.menumusic)
        backgroundMusic?.start()

        backgroundMusic?.setOnCompletionListener { backgroundMusic?.start() }
    }

    private fun playButtonSound() {
        if (buttonSound == null) {
            buttonSound = MediaPlayer.create(applicationContext, R.raw.pulsar_boton)
        }

        buttonSound?.seekTo(0)
        buttonSound?.start()
    }
}