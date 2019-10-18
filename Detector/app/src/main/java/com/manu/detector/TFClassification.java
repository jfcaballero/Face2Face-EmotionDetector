package com.manu.detector;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.manu.detector.utils.FolderUtil;
import com.manu.detector.utils.ImageUtils;

import java.io.File;
import java.io.IOException;


import static java.lang.Thread.sleep;


/** Actividad para mostrar los resultados de la clasificacion
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class TFClassification extends Fragment {

    /** Variable que controla la musica del boton */
    public MediaPlayer mBoton;
    /** Variable que controla la musica correspondiente a la expresion que realicemos */
    public MediaPlayer mExpresion;
    /** Variable que controla la musica de desaparicion de una actividad */
    public MediaPlayer mDesaparecer;
    /** Variable que controla la musica espera al resultado de la clasificacion de la emocion */
    public MediaPlayer mRedobles;
    /** Variable que controla el sonido al realizar incorrectamente la emocion*/
    public MediaPlayer mfallo;
    /** Variable que controla el sonido al realizar correctamente la emocion*/
    public MediaPlayer mAcierto;
    /** Variable que controla la musica del refuerzo con la historia correspondiente*/
    public MediaPlayer mRecuerdaplayer;

    /** Variable que almacena el boton para volver al main, tras una deteccion correcta*/
    public ImageButton volverMain;
    /** Variable que almacena el boton para volver a la actividad anterior (OpenCVCascade) y asi reintentar la emocion, cuando sale el refuerzo*/
    public ImageButton reintentar;
    /** Variable que almacena el boton para volver a la actividad anterior (OpenCVCascade), tras una deteccion correcta*/
    public ImageButton volver;

    /** Variable que almacena la imagen del marco de la foto, aparecera tras una deteccion correcta*/
    public ImageView marco;
    /** Variable que almacena la imagen de la bombilla*/
    public ImageView bombilla;
    /** Variable que almacena el cartel "muy bien"*/
    public ImageView cartelMuyBien;
    /** Variable que almacena la imagen del muelle*/
    public ImageView muelle;
    /** Variable que almacena la imagen del libro*/
    public ImageView recuerdalibro;
    /** Variable que almacena la imagen capturada*/
    public ImageView imagen = null;
    /**Variable que mostrara los resultados de la clasificacion cuando se necesite, solo para el programador*/
    public TextView textView;
    /**Variable que almacena el valor de la clasificacion*/
    public Float confianza;
    /**Variable que almacena el valor y etiqueta de la clasificacion, solo para el programador*/
    public String[] textToShow = new String[2];
    /**Variable que almacena el path para la foto a clasificar*/
    public static String foto;
    /**Variable que almacena el path para la foto a mostrar*/
    public static String foto2;
    /**Variable que almacena el valor de la expresion detectada por OpenCV*/
    public static int exp;
    /**Variable que almacena el numero de epocas*/
    public int count = 0;
    /**Variable que almacena si se acerto al realizar la expresion*/
    public int acierto;
    /**Variable booleana para saber si el clasificador empezo a realizar la deteccion*/
    public boolean runClassifier = false;
    /**Variable que almacena la etiqueta de la clasificacion*/
    public String etiqueta;
    /** Tag de la clase. {@link Log}. */
    public static final String TAG = "TFClassification";
    /** Tag del hilo. {@link Log}. */
    public static final String HANDLE_THREAD_NAME = "DetectionBackground";
    /**Objeto de tipo Bitmap para la imagen a mostrar, este se cambiara en tiempo real por tanto debe ser declarado aqui*/
    public Bitmap b2=null;
    /**Variable que almacena el path de la imagen capturada a mostrar*/
    public File imgFile2;
    /**Clasificador de tipo ImageClassifier*/
    public ImageClassifier classifier;
    /**Objeto de control de hilos*/
    public final Object lock = new Object();
    /**Objeto que almacena un dialogo de espera, se muestra mientras se realiza la clasificacion */
    public ProgressDialog pd = null;

    /** Un hilo adicional para ejecutar tareas en segundo plano y no bloquear el hilo principal */
    public HandlerThread backgroundThread;

    /** Un objeto de tipo {@link Handler} para ejecutar tareas en segundo plano */
    public Handler backgroundHandler;



    /** Esta actividad se encarga de cargar los recursos necesarios para la deteccion
     * @author manu
     * @since 1.0
     * @param savedInstanceState Objeto de tipo Bundle que almacena el estado de la aplicacion al salir de la misma
     * */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            classifier = new ImageClassifier(getActivity());
        } catch (IOException e) {
            Log.e(TAG, "Fallo al iniciar el clasificador.");
        }
        this.pd = ProgressDialog.show(getActivity(), "Procesando", "Espere unos segundos...", true, false); //Espera mientras se clasifica la imagen
        startBackgroundThread(); //Empieza el hilo secundario que realiza la clasificacion
        try {
            sleep(1000);
            musicRedobles(); //Suenan tambores mientras se clasifica
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /** Contructor de la clase, se encarga de reservar los recursos para un objeto de tipo TFClassification
     * @author manu
     * @since 1.0
     * @param idFoto  String con el path de las foto a clasificar
     * @param idFoto2  String con el path de las foto a mostrar
     * @param captada  Entero con el tipo de expresion captada por OpenCV
     * @return TFClassification  Objeto de la clase
     * */
    public static TFClassification newInstance(String idFoto, String idFoto2, int captada) {
        foto=idFoto;
        foto2=idFoto2;
        exp=captada;
        return new TFClassification();
    }


    /** Esta funcion se encarga de reanudar la aplicacion despues de una pausa, reanudando tambien el hilo secundario
     * @author manu
     * @since 1.0
     * */
    public void onResume() {

        startBackgroundThread(); //Empieza el hilo secundario
        super.onResume();

    }

    /** Esta funcion se encarga de cerrar el hilo secundario temporalmente para liberar recursos cuando el usuario no se encuentra en la aplicacion
     * @author manu
     * @since 1.0
     * */
    public void onPause() {


        stopBackgroundThread();
        if(acierto==1){
            if(mAcierto.isPlaying()){
                mAcierto.stop();
            }
            if(mExpresion.isPlaying()){
                mExpresion.stop();
            }
        }else if(acierto==0){
            if(mfallo.isPlaying()){
                mfallo.stop();
            }
            if(mRecuerdaplayer.isPlaying()){
                mRecuerdaplayer.stop();
            }
        }
        super.onPause();

    }

    /** Esta funcion se encarga de liberar los recursos necesarios consumidos por el clasificador
     * @author manu
     * @since 1.0
     * */
    public void onDestroy() {

        classifier.close();
        ImageUtils.eliminarArchivos(foto, foto2);
        super.onDestroy();


    }

    /** Esta funcion se encarga de maquetar la vista previa con los objetos correspondientes al layout
     * @author manu
     * @since 1.0
     * @param inflater  Contiene un objeto de tipo LayoutInflater que se encarga de "hinchar" el layout
     * @param container  Objeto de tipo ViewGroup que contiene un objeto de la vista en la que se muestra el resultado de la clasificacion, solo para el programador
     * @param savedInstanceState Objeto de tipo Bundle que almacena el estado de la aplicacion al salir de la misma
     * @return View  Devuelve la vista
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    /** Esta funcion se encarga de inicializar todos los componentes correspondientes a la vista
     * @author manu
     * @since 1.0
     * @param savedInstanceState Objeto de tipo Bundle que almacena el estado de la aplicacion al salir de la misma
     * @param view  Objeto de tipo View con la vista correspondiente
     * */
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        //Inicializacion de objetos que contienen imagenes, sonidos, botones
        marco= (ImageView) view.findViewById(R.id.marco);
        bombilla= (ImageView) view.findViewById(R.id.bombilla);
        cartelMuyBien= (ImageView) view.findViewById(R.id.cartelMuyBien);
        muelle= (ImageView) view.findViewById(R.id.muelle);
        imagen = (ImageView) view.findViewById(R.id.image);
        recuerdalibro = (ImageView) view.findViewById(R.id.libro);
        volver = (ImageButton) view.findViewById(R.id.botonvolver);
        volverMain = (ImageButton) view.findViewById(R.id.volverAtras);
        reintentar = (ImageButton) view.findViewById(R.id.tryagain);
        textView = (TextView) view.findViewById(R.id.text);

        volverMain.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                bottonMusic();
                musicDesaparecer();
                if(acierto==1){
                    if(mAcierto.isPlaying()){
                        mAcierto.stop();
                    }
                    if(mExpresion.isPlaying()){
                        mExpresion.stop();
                    }
                }else if(acierto==0){
                    if(mfallo.isPlaying()){
                        mfallo.stop();
                    }
                    if(mRecuerdaplayer.isPlaying()){
                        mRecuerdaplayer.stop();
                    }
                }
                Intent cvIntent1 = new Intent(getActivity(), MainActivity.class);
                startActivity(cvIntent1);
            }

        });
        volver.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                bottonMusic();
                musicDesaparecer();
                if(acierto==1){
                    if(mAcierto.isPlaying()){
                        mAcierto.stop();
                    }
                    if(mExpresion.isPlaying()){
                        mExpresion.stop();
                    }
                }else if(acierto==0){
                    if(mfallo.isPlaying()){
                        mfallo.stop();
                    }
                    if(mRecuerdaplayer.isPlaying()){
                        mRecuerdaplayer.stop();
                    }
                }
                Intent cvIntent2 = new Intent(getActivity(), OpenCVCascade.class);
                startActivity(cvIntent2);
            }

        });

        reintentar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                bottonMusic();
                musicDesaparecer();
                if(acierto==1){
                    if(mAcierto.isPlaying()){
                        mAcierto.stop();
                    }
                    if(mExpresion.isPlaying()){
                        mExpresion.stop();
                    }
                }else if(acierto==0){
                    if(mfallo.isPlaying()){
                        mfallo.stop();
                    }
                    if(mRecuerdaplayer.isPlaying()){
                        mRecuerdaplayer.stop();
                    }
                }
                Intent cvIntent2 = new Intent(getActivity(), OpenCVCascade.class);
                startActivity(cvIntent2);
            }

        });



    }


    /** Esta funcion se empezar el hilo secundario {@link Handler}.
     * @author manu
     * @since 1.0
     * */
    public void startBackgroundThread() {
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        synchronized (lock) {
            runClassifier = true;
        }
        //backgroundHandler.post(Classify);
        onPostResult();

    }

    /** Esta funcion se parar el hilo secundario {@link Handler}.
     * @author manu
     * @since 1.0
     * */
    public void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            synchronized (lock) {
                runClassifier = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /** Esta función se encarga de la interacción con el usuario después de recoger el resultado de la clasificación
     * @author manu
     * @since 1.0
     * */
    public void onPostResult() {

        final Runnable Classify = new Runnable() { //Control del hilo secundario
            @Override
            public void run() {
                if(count < 21) {      //epocas de mejora de resultado de clasificacion
                    count++;
                    synchronized (lock) {
                        if (runClassifier) {
                            clasificarImagen();
                            backgroundHandler.postDelayed(this, 0);
                        }
                    }
                    if(count>19){ //Se divide para mejorar el rendimiento y mostrar como va mejorando el resultado
                        if (TFClassification.this.pd != null) {
                            TFClassification.this.pd.dismiss();
                        }

                        if(exp==1){  //Si es alegria
                            if(etiqueta==null){ //Control de errores
                                getActivity().finish();
                            }
                            exp=0;
                            if(etiqueta.equals("smileimg") && (confianza > 0.45)) { //Acierto Alegria
                                //Realiza lo correspondiente segun resultado en el hilo principal
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        imagen.setVisibility(imagen.VISIBLE);
                                        marco.setVisibility(marco.VISIBLE);
                                        bombilla.setVisibility(bombilla.VISIBLE);
                                        cartelMuyBien.setVisibility(cartelMuyBien.VISIBLE);
                                        muelle.setVisibility(muelle.VISIBLE);

                                    }
                                });

                                musicExp(1);
                                musicGenteAcierto();
                                acierto=1; //Variable de control
                                Log.i("sonrisa", "·es sonrisa ");


                            } else if(etiqueta.equals("neutralimg")){ //No acierto alegria pero casi

                                Log.i("neutral", "·Trata de sonreir mas ");
                                acierto=0;
                                //Realiza lo correspondiente segun resultado en el hilo principal
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        errorExpresion();
                                        try {
                                            sleep(600);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        vozRecuerda(1);
                                        recuerdalibro.setImageResource(R.drawable.libro_historia_maria1);
                                        recuerdalibro.setVisibility(recuerdalibro.VISIBLE);
                                        reintentar.setVisibility(reintentar.VISIBLE);
                                        volverMain.setClickable(false);
                                        volver.setClickable(false);

                                    }
                                });

                            }else{ //No acierto alegria ni de cerca

                                Log.i("surprisedoangry", "·No estas realizando la expresion correctamente ");
                                acierto=0;
                                //Realiza lo correspondiente segun resultado en el hilo principal
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        errorExpresion();
                                        try {
                                            sleep(600);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        vozRecuerda(2);
                                        recuerdalibro.setImageResource(R.drawable.libro_historia_maria2);
                                        recuerdalibro.setVisibility(recuerdalibro.VISIBLE);
                                        reintentar.setVisibility(reintentar.VISIBLE);
                                        volverMain.setClickable(false);
                                        volver.setClickable(false);


                                    }
                                });


                            }
                        } else if(exp==2){ //SI es sorpresa
                            if(etiqueta==null){ //Control de errores
                                getActivity().finish();
                            }
                            exp=0;
                            if(etiqueta.equals("surprisedimg") && (confianza > 0.79)) { //Acierto sorpresa, antes 0,82

                                //Realiza lo correspondiente segun resultado en el hilo principal
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        imagen.setVisibility(imagen.VISIBLE);
                                        marco.setVisibility(marco.VISIBLE);
                                        bombilla.setVisibility(bombilla.VISIBLE);
                                        cartelMuyBien.setVisibility(cartelMuyBien.VISIBLE);
                                        muelle.setVisibility(muelle.VISIBLE);

                                    }
                                });

                                musicExp(2);
                                musicGenteAcierto();
                                acierto=1;
                                Log.i("sopresa", "·es sopresa ");

                            }else if(etiqueta.equals("neutralimg")){  //No acierto sorpresa pero casi

                                Log.i("neutral", "·Trata de sonreir mas ");
                                acierto=0;
                                //Realiza lo correspondiente segun resultado en el hilo principal
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        errorExpresion();
                                        try {
                                            sleep(600);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        vozRecuerda(3);

                                        recuerdalibro.setImageResource(R.drawable.libro_historia_javier1);
                                        recuerdalibro.setVisibility(recuerdalibro.VISIBLE);
                                        reintentar.setVisibility(reintentar.VISIBLE);
                                        volverMain.setClickable(false);
                                        volver.setClickable(false);

                                    }
                                });

                            }else{ //No acierto ni de cerca

                                Log.i("smileoangry", "·No estas realizando la expresion correctamente ");
                                acierto=0;
                                //Realiza lo correspondiente segun resultado en el hilo principal
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        errorExpresion();
                                        try {
                                            sleep(600);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        vozRecuerda(4);
                                        recuerdalibro.setImageResource(R.drawable.libro_historia_javier2);
                                        recuerdalibro.setVisibility(recuerdalibro.VISIBLE);
                                        reintentar.setVisibility(reintentar.VISIBLE);
                                        volverMain.setClickable(false);
                                        volver.setClickable(false);

                                    }
                                });

                            }
                        }
                    }
                }
            }
        };

        backgroundHandler.post(Classify); //Una vez mas
        //stopBackgroundThread();

    }


    /** Esta funcion se encarga de preprocesar la imagen y de llamar a la funcion correspondiente para realizar la clasificacion, asi como se llamar a la funcion que almacena el resultado de la clasificacion
     * @author manu
     * @since 1.0
     * */
    public void clasificarImagen() {

        //Control de errores
        if (classifier == null || getActivity() == null) {
            System.exit(1);
            return;
        }

        //Imagen 1 -> Procesar, es el crop de cara en gris
        //Imagen 2 -> Mostrar, es la captura normal
        File imgFile = new File(foto);
        imgFile2 = new File(foto2);

        if(imgFile.exists() && imgFile2.exists()){

            //Convertirmos a bitmap para procesasrla
            Bitmap b = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            //En el hilo principal se fija la imagen a mostrar
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    b2 = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
                    imagen.setImageBitmap(b2);
                }
            });

            //Se redimensiona la imagen en bitmap dado que el modelo esta preparado para trabajar con imagenes de 224x224
            b = ImageUtils.getResizedBitmap(b, 224, 224);

            for(int i=0;i<2;i++) { //Se muestra el resultado de cada mejora cada 2 iteraciones
                textToShow = classifier.classify(b);  //Clasifica la imagen
            }
            b.recycle(); //Libera memoria del objeto bitmap
            storeResult(textToShow); //Se almacena el resultado

        }
    }


    /** Esta funcion se encarga de almacenar el resultado de la clasificacion
     * @author manu
     * @since 1.0
     * @param text  Objeto de tipo vector de String que almacena el resultado de la clasificacion, solo para el programador
     * */
    public void storeResult(final String[] text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            etiqueta=text[0]; //Etiqueta
                            confianza=Float.valueOf(text[1]); //Confianza del resultado
                            textView.setText(text[0]+" - "+ text[1]); //Solo para el programador
                        }
                    });
        }
    }


    /** Esta funcion controla la musica del programa cuando fallamos
     * @author manu
     * @since 1.0
     * @param i  Variable de control, para saber el fallo que se cometio y proporcionar le refuerzo correspondiente
     * */
    public void vozRecuerda(int i){

        switch (i){
            case 1:
                this.mRecuerdaplayer = MediaPlayer.create(getActivity(), R.raw.recuerdamaria2);
                this.mRecuerdaplayer.seekTo(0);
                this.mRecuerdaplayer.start();
                break;
            case 2:
                this.mRecuerdaplayer = MediaPlayer.create(getActivity(), R.raw.recuerdamaria);
                this.mRecuerdaplayer.seekTo(0);
                this.mRecuerdaplayer.start();
                break;
            case 3:
                this.mRecuerdaplayer = MediaPlayer.create(getActivity(), R.raw.recuerdajavier2);
                this.mRecuerdaplayer.seekTo(0);
                this.mRecuerdaplayer.start();
                break;
            case 4:
                this.mRecuerdaplayer = MediaPlayer.create(getActivity(), R.raw.recuerdajavier);
                this.mRecuerdaplayer.seekTo(0);
                this.mRecuerdaplayer.start();
                break;

        }
        mRecuerdaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mRecuerdaPlayer) {
                mRecuerdaplayer.reset();

            };
        });

    }

    /** Esta funcion se encarga del sonido de aplausos cuando se acierta
     * @author manu
     * @since 1.0
     * */
    public void musicGenteAcierto(){

        this.mAcierto = MediaPlayer.create(getActivity(), R.raw.aplausos);
        this.mAcierto.seekTo(0);
        this.mAcierto.start();

        mAcierto.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mAcierto) {
                mAcierto.reset();

            };
        });


    }

    /** Esta funcion se encarga del sonido de los redobles mientras se espera a realizar la clasificacion
     * @author manu
     * @since 1.0
     * */
    public void musicRedobles(){

        this.mRedobles = MediaPlayer.create(getActivity(), R.raw.redobleyplatillos);
        this.mRedobles.seekTo(0);
        this.mRedobles.start();

        mRedobles.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mRedobles) {
                mRedobles.release();

            };
        });
    }

    /** Esta funcion se encarga de la musica al desaparecer el layout correspondiente a la actividad
     * @author manu
     * @since 1.0
     * */
    public void musicDesaparecer(){

        this.mDesaparecer = MediaPlayer.create(getActivity(), R.raw.violin_desaparecer);
        this.mDesaparecer.seekTo(0);
        this.mDesaparecer.start();

        mDesaparecer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mDesaparecer) {
                mDesaparecer.reset();

            };
        });

    }

    /** Esta funcion se encarga del sonido de los botones
     * @author manu
     * @since 1.0
     * */
    public void bottonMusic(){

        this.mBoton = MediaPlayer.create(getActivity(), R.raw.pulsar_boton);
        this.mBoton.seekTo(0);
        this.mBoton.start();

        mBoton.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mBoton) {
                mBoton.release();

            };
        });

    }
    

    /** Esta funcion se encarga del sonido cuando se acierta la expresion
     * @author manu
     * @since 1.0
     * */

    public void musicExp(int i) {

        if(i==2) {

            this.mExpresion = MediaPlayer.create(getActivity(), R.raw.muybiensorpresa);
            this.mExpresion.seekTo(0);
            this.mExpresion.start();

        }else{

            this.mExpresion = MediaPlayer.create(getActivity(), R.raw.muybienalegria);
            this.mExpresion.seekTo(0);
            this.mExpresion.start();

        }

        mExpresion.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mExpresion) {
                mExpresion.reset();

            };
        });

    }

    /** Esta funcion se encarga del sonido para proporcionar un aviso cuando se falla la expresion
     * @author manu
     * @since 1.0
     * */
    public void errorExpresion(){

        this.mfallo = MediaPlayer.create(getActivity(), R.raw.errorohh);
        this.mfallo.seekTo(0);
        this.mfallo.start();

        mfallo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mfallo) {
                mfallo.reset();

            };
        });

    }

}
