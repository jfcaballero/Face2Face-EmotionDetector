package com.manu.detector;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;


/** Actividad de la visualizacion de las historias
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class loadStory extends AppCompatActivity{

    /** Variable que almacena el boton para iniciar el video */
    public ImageButton btnPlay;
    /** Variable que controla el paso entre actividades */
    public ImageButton btnStop;
    /** Variable que controla la musica del boton */
    public MediaPlayer mBoton;
    /** Variable que controla la musica de encendido de la television */
    public MediaPlayer mTV;
    /** Variable que controla la musica de aparicion de una actividad */
    public MediaPlayer mAparecer;
    /** Variable que controla la musica de desaparicion de una actividad */
    public MediaPlayer mDesaparecer;
    /** Variable que almacena el video con la historia alegria */
    public VideoView videoA;
    /** Variable que almacena el video con la historia sorpresa*/
    public VideoView videoS;
    /** Variable que controla la decision del usuario sobre la historia que quiere ver */
    public int video;
    /** Variable que controla la decision del usuario sobre la historia que quiere ver */
    public int mute;

    /** Esta actividad se encarga de controlar las animaciones que muestran la historia para el aprendizaje de la emocion
     * @author manu
     * @since 1.0
     * @param savedInstanceState Objeto de tipo Bundle que almacena el estado de la aplicacion al salir de la misma
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions =   View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //Se asigna el layout correspondiente
        setContentView(R.layout.video);

        mute = getIntent().getIntExtra("mute", 0);

        //Musica al iniciar la actividad
        if(mute == 0) musicAparecer();

        //Se recupera el valor de la varible pasada desde el Main para saber que historia se eligio
        video = getIntent().getIntExtra("video", 0);

        //Se carga el video correspondiente
        eleccionVideo(video);


        //Boton de iniciar la historia
        btnPlay = findViewById(R.id.buttonPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(video == 2){
                    if(mute == 0) musicTVencendida(); //Musica de encendido de la Tele
                    videoS.start(); //Empieza el video sorpresa
                }else{
                    if(mute == 0) musicTVencendida();
                    videoA.start(); //Empieza el video alegria
                }
            }


        });


        //Boton de parar la historia
        btnStop = findViewById(R.id.buttonStop);
        btnStop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(video == 2){
                    videoS.stopPlayback(); //Boton de parar el video sorpresa si se sale de la aplicacion
                }else{
                    videoA.stopPlayback(); //Boton de parar el video alegria si se sale de la aplicacion
                }
                if(mute == 0) bottonMusic();
                if(mute == 0) musicDesaparecer(); //Musica de cambiar de actividad
                Intent cvIntent = new Intent(loadStory.this, MainActivity.class); //Se le pasa el control a la actividad Main
                if(video == 2) {
                    cvIntent.putExtra("terminado", 2);  //Se le pasa el valor de la variable para saber que puede acceder al otro modulo porque vio las historias y aprendio a realizar la expresion sorpresa
                }else{
                    cvIntent.putExtra("terminado", 1);  //Se le pasa el valor de la variable para saber que puede acceder al otro modulo porque vio las historias y aprendio a realizar la expresion alegria
                }
                startActivity(cvIntent);
            }
        });
    }

    /** Esta funcion se encarga de reanudar la aplicacion despues de una pausa, ademas de inicializar los recursos necesarios para el correcto funcionamiento de la aplicacion
     * @author manu
     * @since 1.0
     * */
    public void onResume() {

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onResume();
        mute = getIntent().getIntExtra("mute", 0);
        video = getIntent().getIntExtra("video", 0);
    }

    /** Esta funcion se encarga de la seleccion del video a mostrar al usuario
     * @author manu
     * @since 1.0
     * */
    public void eleccionVideo(int video){

        if (video == 1) { //Si el valor de la variable de entrada es 1 -> alegria

            videoA = findViewById(R.id.videoView);
            String path = "android.resource://" + getPackageName() + "/" + R.raw.alegria;
            videoA.setVideoURI(Uri.parse(path));

        } else if(video==2){ //Si el valor de la variable de entrada es 2 -> sorpresa

            videoS = findViewById(R.id.videoView);
            String path = "android.resource://" + getPackageName() + "/" + R.raw.sorpresa;
            videoS.setVideoURI(Uri.parse(path));

        }

    }

    /** Esta funcion se encarga de la musica al aparecer el layout correspondiente a la actividad
     * @author manu
     * @since 1.0
     * */
    public void musicAparecer(){

        this.mAparecer = MediaPlayer.create(this, R.raw.violin_aparecer);
        this.mAparecer.seekTo(0);
        this.mAparecer.start();
        mAparecer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mAparecer) {
                mAparecer.reset();

            }
        });

    }

    /** Esta funcion se encarga de la musica al desaparecer el layout correspondiente a la actividad
     * @author manu
     * @since 1.0
     * */
    public void musicDesaparecer(){

        this.mDesaparecer = MediaPlayer.create(this, R.raw.violin_desaparecer);
        this.mDesaparecer.seekTo(0);
        this.mDesaparecer.start();
        mDesaparecer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mDesaparecer) {
                mDesaparecer.reset();

            }
        });

    }

    /** Esta funcion se encarga de la musica al pulsar un boton de la actividad
     * @author manu
     * @since 1.0
     * */
    public void bottonMusic(){

        this.mBoton = MediaPlayer.create(this, R.raw.pulsar_boton);
        this.mBoton.seekTo(0);
        this.mBoton.start();
        mBoton.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mBoton) {
                mBoton.release();

            }
        });

    }

    /** Esta funcion se encarga de reproducir un sonido al pulsar el boton de iniciar el video de la historia
     * @author manu
     * @since 1.0
     * */
    public void musicTVencendida(){

        this.mTV = MediaPlayer.create(this, R.raw.tvon);
        this.mTV.seekTo(0);
        this.mTV.start();
        mTV.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mTV) {
                mTV.release();

            }
        });


    }

}
