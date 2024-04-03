package com.miguelangel.face2facev2

import android.content.Intent
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

    private var videoStopPos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        Utils.hideSystemBars(this)

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
            video.setOnCompletionListener {
                video.stopPlayback()
                val context = this
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("mute", mute)
                context.startActivity(intent)
            }
        })

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
    }


    override fun onPause() {
        super.onPause()
        if (video.isPlaying) {
            videoStopPos = video.currentPosition
            video.pause()

        }
    }

    override fun onResume() {
        super.onResume()
        video.seekTo(videoStopPos)
        video.resume()
    }

}