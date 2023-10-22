package com.miguelangel.face2facev2

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.VideoView

class StoryActivity : AppCompatActivity() {
    private var mute: Boolean = false

    private var buttonSound: MediaPlayer? = null

    private var violinSound: MediaPlayer? = null

    private var tvSound: MediaPlayer? = null

    private lateinit var volverButton: ImageButton

    private lateinit var playButton: ImageButton

    private lateinit var video: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        Utils.setWindowPresentation(this)

        val storyId = intent?.extras?.getInt("storyId")

        val videoPath = "android.resource://${packageName}/${if(storyId == 0) R.raw.alegria else R.raw.sorpresa}"

        video = findViewById(R.id.videoView)
        video.setVideoURI(Uri.parse(videoPath))

        val muteIntent = intent?.extras?.getBoolean("mute")
        if(muteIntent != null)
            mute = muteIntent

        if(!mute)
            playViolinSound(0)

        playButton = findViewById(R.id.buttonPlay)

        playButton.setOnClickListener(View.OnClickListener {
            if (!mute)
                playTvSound()
            video.start()
        })

        volverButton = findViewById(R.id.buttonVolver)

        volverButton.setOnClickListener(View.OnClickListener {
            if(!mute) {
                playButtonSound()
                playViolinSound(1)
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

    private fun playButtonSound() {
        buttonSound = MediaPlayer.create(applicationContext, R.raw.pulsar_boton)

        buttonSound?.seekTo(0)
        buttonSound?.start()
        buttonSound?.setOnCompletionListener {
            buttonSound?.release()
        }
    }

    private fun playViolinSound(type: Int) {
        violinSound = MediaPlayer.create(applicationContext, if(type == 0) R.raw.violin_aparecer else R.raw.violin_desaparecer)

        violinSound?.start()
        violinSound?.setOnCompletionListener {
            violinSound?.release()
        }
    }

    private fun playTvSound() {
        tvSound = MediaPlayer.create(applicationContext, R.raw.tvon)

        tvSound?.start()
        tvSound?.setOnCompletionListener {
            tvSound?.release()
        }
    }
}