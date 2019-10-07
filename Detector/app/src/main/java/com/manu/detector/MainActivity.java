package com.manu.detector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

/** Actividad principal de la aplicacion, se encarga del control entre las distintas actividades
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class MainActivity extends AppCompatActivity {

    /** Controla el paso entre actividades */
    public Intent cvIntent2;
    /** Variable de control de la musica del programa */
    public MediaPlayer mPlayer;
    /** Variable de control de la musica de los botones. */
    public MediaPlayer mBoton;
    /** Variable que almacena el boton para ir al modulo de práctica */
    public ImageButton reconocimientoOpenCV;


    /** Esta funcion se encarga de la creacion de la actividad
     * @author manu
     * @since 1.0
     * @param savedInstanceState Objeto de tipo Bundle que almacena el estado de la aplicacion al salir de la misma
     * */
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Se asigna el layout correspondiente
        setContentView(R.layout.activity_main);
        //Se activa la musica
        music();

        //Se pregunta si acepta los permisos (Camara y lectura/escritura en el almacenamiento)
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(MainActivity.this, "You need to grant all permission to use this app features", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();


        //Se recupera el valor de las variables de otras actividades
        int aMostrar= (int) getIntent().getIntExtra("var", 0); //Se inicializa el menu o la actividad TFClass
        int terminado = getIntent().getIntExtra("terminado", 0); //Para ver si vio los videos antes

        //Se muestra la actividad TFClassification (Esto es porque viene de la actividad OpenCVCascade)
        if(aMostrar==1){

            //Se recupera el valor de la expresion que detecto la actividad de OpenCV
            int exp= getIntent().getIntExtra("exp", 0); // 1 es sonrisa 2 es sorpresa
            //Tambien se recupera el path en el cual se almaceno la imagen (movil)
            String foto= getIntent().getStringExtra("foto");
            String foto2= getIntent().getStringExtra("foto2");
            //Se asigna el layout correspondiente
            setContentView(R.layout.fragment_camera2_basic);

            //Se inicializa y se pasa el control a la actividad pasandole le path de las fotos y el valor de la expresion captada por la clase OpenCV
            if (null == savedInstanceState) {
                if(mPlayer.isPlaying()){
                    mPlayer.stop(); //Paramos la musica del menu
                }
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, TFClassification.newInstance(foto, foto2, exp))
                        .commit();

            }

        // Se muestra el menu principal
        }else{

            //Si el usuario ha visto los videos
            if(terminado==1){

            //Se le permite acceder al modulo de reconocimiento activando el boton
            reconocimientoOpenCV = (ImageButton) findViewById(R.id.reconocimient);
            reconocimientoOpenCV.setImageResource(R.drawable.boton);
            reconocimientoOpenCV.setClickable(true);
                reconocimientoOpenCV.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        if(mPlayer.isPlaying()){
                            mPlayer.stop(); //Paramos la musica del menu
                        }
                        bottonMusic(); //Musica de interaccion con el boton
                        Intent cvIntent1 = new Intent(MainActivity.this, OpenCVCascade.class);
                        startActivity(cvIntent1);
                    }

                });

            }

            //Se le permite acceder al modulo de aprendizaje activando los botones correspondientes a las historias. La misma clase controlara la carga de los videos segun el valor de la variable video que se le pasa
            ImageButton alegria = (ImageButton) findViewById(R.id.historiaAlegri);
            alegria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mPlayer.isPlaying()){
                        mPlayer.stop(); //Paramos la musica del menu
                    }
                    bottonMusic(); //Musica de interaccion con el boton
                    cvIntent2 = new Intent(MainActivity.this, cargaVideo.class); //Se le pasa el control a la actividad cargaVideo
                    cvIntent2.putExtra("video", 1); //Se le pasa el valor de la variable video (1=alegria)
                    startActivity(cvIntent2);
                }

            });

            ImageButton sorpresa = (ImageButton) findViewById(R.id.historiaSorpres);
            sorpresa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mPlayer.isPlaying()){
                        mPlayer.stop(); //Paramos la musica del menu
                    }
                    bottonMusic();
                    cvIntent2 = new Intent(MainActivity.this, cargaVideo.class);
                    cvIntent2.putExtra("video", 2); //Se le pasa el valor de la variable video (2=sorpresa)
                    startActivity(cvIntent2);
                }

            });


        }

    }

    /** Esta funcion se encarga de reanudar la aplicacion despues de una pausa
     * @author manu
     * @since 1.0
     * */
    public void onResume(){

        mPlayer.seekTo(0);
        mPlayer.start();
        super.onResume();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Se obliga a utilizar el telefono en modo horizontal

    }

    /** Esta funcion se encarga de liberar la memoria consumida por la aplicacion tras su cierre
     * @author manu
     * @since 1.0
     * */
    public void onDestroy() {

        super.onDestroy();

    }

    /** Esta funcion se encarga de liberar la memoria consumida por la aplicacion tras su cierre
     * @author manu
     * @since 1.0
     * */
    public void onPause() {

        if(mPlayer.isPlaying()){
            mPlayer.pause();
        }
        super.onPause();

    }

    /** Esta funcion se encarga de controlar la musica principal del programa
     * @author manu
     * @since 1.0
     * */
    public void music(){

        this.mPlayer = MediaPlayer.create(this, R.raw.menumusic);
        this.mPlayer.seekTo(0);
        this.mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.reset();
                //mPlayer.release();

            };
        });

    }

    /** Esta funcion se encarga de controlar la musica al pulsar un boton de esta pantalla
     * @author manu
     * @since 1.0
     * */
    public void bottonMusic(){

        this.mBoton = MediaPlayer.create(this, R.raw.pulsar_boton);
        this.mBoton.seekTo(0);
        this.mBoton.start();
        mBoton.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mBoton) {
                mBoton.reset();
                //mBoton.release();

            };
        });
    }
}


