package com.miguelangel.face2facev2

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.VideoView

class StoryActivity : AppCompatActivity() {
    private var mute: Boolean = false

    private lateinit var volverButton: ImageButton

    private lateinit var playButton: ImageButton

    private lateinit var video: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        Utils.setWindowPresentation(this)

        val videoId = intent?.extras?.getInt("videoId")

        val videoPath = "android.resource://${packageName}/${videoId}"

        video = findViewById(R.id.videoView)
        video.setVideoURI(Uri.parse(videoPath))

        mute = intent?.extras?.getBoolean("mute") ?: false

        if(!mute)
            Utils.playSound(applicationContext, R.raw.violin_aparecer)

        playButton = findViewById(R.id.buttonPlay)

        playButton.setOnClickListener(View.OnClickListener {
            if (!mute)
                Utils.playSound(applicationContext, R.raw.tvon)
            video.start()
        })

        volverButton = findViewById(R.id.buttonVolver)

        volverButton.setOnClickListener(View.OnClickListener {
            if(!mute) {
                Utils.playSound(applicationContext, R.raw.pulsar_boton)
                Utils.playSound(applicationContext, R.raw.violin_desaparecer)
            }

            /*
            Preguntar a Juan Carlos si aqui se debe volver a MainActivity indicando si el usuario ha visto los videos completos.
            En el codigo de Manuel Perez esto estaba hecho pero las lineas que lo hacen estan comentadas.
            */
            finish()
        })
    }

    /*
    override fun onPause() {
        super.onPause()
        if (video.isPlaying) {
            video.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        video.resume()
    }
     */
}