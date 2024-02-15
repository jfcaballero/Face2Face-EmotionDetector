package com.miguelangel.face2facev2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    private var backgroundMusic: MediaPlayer? = null

    private var mute: Boolean = false

    private lateinit var sonidoButton: ImageButton

    private lateinit var configButton: ImageButton

    private lateinit var creditos: ImageView

    private lateinit var historiaSorpresa: ImageButton

    private lateinit var historiaAlegria: ImageButton

    private lateinit var practicaSorpresa: ImageButton

    private lateinit var practicaAlegria: ImageButton

    private lateinit var preferences: SharedPreferences

    private lateinit var configDialog: ConfiguracionDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Utils.setWindowPresentation(this)
        Utils.requestPermission(this)

        mute = intent?.extras?.getBoolean("mute") ?: false

        preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        sonidoButton = findViewById(R.id.sonido)
        if (!mute) {
            sonidoButton.setImageResource(R.drawable.vol_up)
            playBackgroundMusic()
        }
        else {
            sonidoButton.setImageResource(R.drawable.vol_mute)
        }

        // Se activa un listener para poder poner la aplicacion en mute
        sonidoButton.setOnClickListener(View.OnClickListener {
            if (!mute) {
                sonidoButton.setImageResource(R.drawable.vol_mute)
                mute = true

                if (backgroundMusic?.isPlaying == true) {
                    backgroundMusic?.pause()
                }
            }
            else {
                sonidoButton.setImageResource(R.drawable.vol_up)
                mute = false
                if (backgroundMusic != null) {
                    backgroundMusic?.start()
                }
                else {
                    playBackgroundMusic()
                }
            }
        })

        configDialog = ConfiguracionDialogFragment(preferences)

        configButton = findViewById(R.id.config)
        configButton.setOnClickListener (View.OnClickListener {
            configDialog.show(supportFragmentManager, "Configuracion")
        })


        historiaAlegria = findViewById(R.id.historiaAlegria)
        setStoryListener(historiaAlegria, R.raw.alegria)
        historiaSorpresa = findViewById(R.id.historiaSorpresa)
        setStoryListener(historiaSorpresa, R.raw.sorpresa)

        practicaAlegria = findViewById(R.id.practicaAlegria)
        setPracticeListener(practicaAlegria, 0)
        practicaSorpresa = findViewById(R.id.practicaSorpresa)
        setPracticeListener(practicaSorpresa, 1)


        creditos = findViewById(R.id.creditos)
        val creditosButton = findViewById<ImageButton>(R.id.buttonCreditos)

        creditosButton.setOnClickListener(View.OnClickListener {
            if (!mute)
                Utils.playSound(applicationContext, R.raw.pulsar_boton)

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

    private fun setStoryListener(button: ImageButton, videoId: Int) {
        button.setOnClickListener(View.OnClickListener {
            if(!mute)
                Utils.playSound(applicationContext, R.raw.pulsar_boton)
            val context = button.context
            val intent = Intent(context, StoryActivity::class.java)
            intent.putExtra("videoId", videoId)
            intent.putExtra("mute", mute)
            context.startActivity(intent)
        })
    }

    private fun setPracticeListener(button: ImageButton, id: Int) {
        button.setOnClickListener(View.OnClickListener {
            if(!mute)
                Utils.playSound(applicationContext, R.raw.pulsar_boton)
            val context = button.context
            val intent = Intent(context, DetectorActivity::class.java)
            intent.putExtra("emotionId", id)
            intent.putExtra("mute", mute)
            context.startActivity(intent)
        })
    }

    private fun playBackgroundMusic() {
        backgroundMusic = MediaPlayer.create(applicationContext, R.raw.menumusic)
        backgroundMusic?.start()

        backgroundMusic?.setOnCompletionListener { backgroundMusic?.start() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(this)
        }
    }
}