package com.uco.face2facev2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.Image
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private var backgroundMusic: MediaPlayer? = null

    private var mute: Boolean = false

    private lateinit var sonidoButton: ImageButton

    private lateinit var configButton: ImageButton

    private lateinit var helpButton: ImageButton

    private lateinit var salirButton: ImageButton

    private lateinit var scrollCreditos: ScrollView

    private lateinit var historiaSorpresa: ImageButton

    private lateinit var historiaAlegria: ImageButton

    private lateinit var practicaSorpresa: ImageButton

    private lateinit var practicaAlegria: ImageButton

    private lateinit var preferences: SharedPreferences

    private lateinit var configDialog: ConfiguracionDialogFragment

    private lateinit var helpDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Utils.hideSystemBars(this)
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

        salirButton = findViewById(R.id.buttonSalir)
        salirButton.setOnClickListener {
            finishAffinity()
            exitProcess(0)
        }

        configDialog = ConfiguracionDialogFragment(preferences)

        configButton = findViewById(R.id.config)
        configButton.setOnClickListener (View.OnClickListener {
            configDialog.show(supportFragmentManager, "Configuracion")
        })

        val helpDialogView = layoutInflater.inflate(R.layout.help_dialog, null) as ScrollView
        val textoAyuda = helpDialogView.findViewById<TextView>(R.id.texto_ayuda)
        textoAyuda.text = Html.fromHtml(getString(R.string.ayuda), FROM_HTML_MODE_LEGACY)
        val builder = AlertDialog.Builder(this)
        builder
            .setView(helpDialogView)
            .setTitle("Ayuda")
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
        helpDialog = builder.create()

        helpButton = findViewById(R.id.help)
        helpButton.setOnClickListener (View.OnClickListener {
            helpDialog.show()
        })

        historiaAlegria = findViewById(R.id.historiaAlegria)
        setStoryListener(historiaAlegria, R.raw.alegria)
        historiaSorpresa = findViewById(R.id.historiaSorpresa)
        setStoryListener(historiaSorpresa, R.raw.sorpresa)

        practicaAlegria = findViewById(R.id.practicaAlegria)
        setPracticeListener(practicaAlegria, 0)
        practicaSorpresa = findViewById(R.id.practicaSorpresa)
        setPracticeListener(practicaSorpresa, 1)

        scrollCreditos = findViewById(R.id.scroll)
        val creditosButton = findViewById<ImageButton>(R.id.buttonCreditos)
        val closeCreditos = findViewById<ImageButton>(R.id.closeCreditos)

        creditosButton.setOnClickListener(View.OnClickListener {
            if (!mute)
                Utils.playSound(applicationContext, R.raw.pulsar_boton)

            if (scrollCreditos.visibility != View.VISIBLE) {
                scrollCreditos.visibility = View.VISIBLE
                closeCreditos.visibility = View.VISIBLE
                closeCreditos.isClickable = true
            }

            else {
                scrollCreditos.visibility = View.GONE
                closeCreditos.visibility = View.GONE
                closeCreditos.isClickable = false
            }

        })

        closeCreditos.setOnClickListener {
            scrollCreditos.visibility = View.GONE
            it.visibility = View.GONE
            it.isClickable = false
        }

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
        if (!mute) {
            backgroundMusic?.start()
        }
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
            finishAndRemoveTask()
            exitProcess(1)
        }
    }
}