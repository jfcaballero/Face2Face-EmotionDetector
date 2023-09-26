package com.manu.detector;


import static java.lang.Thread.sleep;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.manu.detector.utils.FolderUtil;
import com.manu.detector.utils.ImageUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;

/** Esta actividad se encarga de detectar la cara y la emocion del niño y si es correcta de capturar una fotografia para posteriormente clasificarla
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class OpenCVCascade extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    /** Tag de la clase. {@link Log}. */
    public static final String TAG = "OpenCVCascade";

    /** Almacena un objeto de tipo CascadeClassifier con el modelo para la deteccion de la cara */
    public CascadeClassifier cara = null; //
    /** Almacena un objeto de tipo CascadeClassifier con el modelo para la deteccion de la sonrisa */
    public CascadeClassifier smile = null;
    /** Almacena un objeto de tipo CascadeClassifier con el modelo para la deteccion de la expresion de sorpresa */
    public CascadeClassifier surprise = null;  //
    /** Almacena un objeto de tipo JavaCameraView con la visualizacion de la camara del dispositivo */
    public JavaCameraView camara;
    /** Variable que controla la musica de aparicion de una actividad */
    public MediaPlayer mAparecer;
    /** Variable que controla la musica de desaparicion de una actividad */
    public MediaPlayer mDesaparecer;
    /** Variable que almacena el sonido de realizar una captura */
    public MediaPlayer mCamara;
    /** Variable que almacena el sonido al pulsar un boton */
    public MediaPlayer mBoton;
    /** Variable que almacena el sonido de la voz de la cara que tienes que poner */
    public MediaPlayer mPonerCara;
    /** Variable que almacena el una imagen de la cara animada */
    public ImageView imagencara;   //Imagen animada cara
    /** Variable que almacena el boton para volver */
    public ImageButton volver;
    /** Objeto Mat que almacena cada frame de la camra en gris */
    public Mat gray;
    /** Variable que controla el flujo del usuario (video que visualizo el usuario) */
    public int videoElegido;
    /** Variable que controla el sonido de la app */
    public int mute;


    /** Esta funcion Se encarga de inicializar la libreria y el modelo que se encarga de la deteccion
     * @author manu
     * @since 1.0
     * @param status Variable que almacena si la libreria de OpenCV se cargo correctamente al proyecto
     * */

    public BaseLoaderCallback ocvLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: //Cuando la carga de la libreria fue correcta

                    Log.i(TAG, "Carga de OpenCV correcta...");
                    camara.enableView();

                    //Carga de los clasificadores en cascada
                    String path = CascadeHelper.generateXmlPath(this.mAppContext, R.raw.haarcascade_frontalface, 1);
                    String path2 = CascadeHelper.generateXmlPath(this.mAppContext,R.raw.haarcascade_smile, 2);
                    String path3 = CascadeHelper.generateXmlPath(this.mAppContext,R.raw.haarcascade_surprise,3);

                    //System.out.println(" el path del primero es " + path + R.raw.haarcascade_frontalface);
                    //System.out.println(" el path del segundo es " + path2 + R.raw.haarcascade_smile);
                    //System.out.println(" el path del tercero es " + path3 + R.raw.haarcascade_surprise);

                    //Inicializacion de los clasificadores en cascada
                    if (path != null && path2 != null && path3 != null) {
                        cara = new CascadeClassifier(path);
                        smile = new CascadeClassifier(path2);
                        surprise = new CascadeClassifier(path3);

                        //Control de errores
                        if (!cara.empty()) {
                            Log.i(TAG, "Cargar clasificador .xml, archivo: " + path);
                        }

                        if (!smile.empty()) {
                            Log.i(TAG, "Cargar clasificador .xml, archivo: " + path2);
                        }

                        if (!surprise.empty()) {
                            Log.i(TAG, "Cargar clasificador .xml, archivo: " + path3);
                        }
                    }


                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    /** Este metodo se encarga de iniciar la actividad correspondiente y de cargar los recursos que sean necesarios
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
        //Mantiene la pantalla encendida
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mute = getIntent().getIntExtra("mute", 0);
        if(mute == 0) musicAparecer();

        /*
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) { // Comprobamos que tenemos permisos, si no tenemos permisos...


        ActivityCompat.requestPermissions(this,     //Pedimos los permisos para utilizar la camara
                new String[]{Manifest.permission.CAMERA}, 1);

        }

        //requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);

        */
        //Se asigna el layout correspondiente
        setContentView(R.layout.activity_detector);
        //Recupero el valor entero del video que visualizó el usuario
        videoElegido = getIntent().getIntExtra("videoElegido", 0);


        vozPonerExpresion(videoElegido);

        //Se inicializan la camara con el View del layout correspondiente
        camara = new JavaCameraView(this, CameraBridgeViewBase.CAMERA_ID_FRONT);
        camara = findViewById(R.id.camera_view);
        camara.setVisibility(SurfaceView.VISIBLE);
        camara.setCvCameraViewListener(this);
        //camara.disableFpsMeter();

        //Se inicializa el boton de volver  a la actividad Main
        volver = findViewById(R.id.volverAtras);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mute == 0) {
                    musicDesaparecer();
                    bottonmusic();
                }
                Intent cvIntent2 = new Intent(OpenCVCascade.this, MainActivity.class);
                cvIntent2.putExtra("terminado", 1);
                startActivity(cvIntent2);
            }

        });

    }

    /** Esta funcion se encarga de reanudar la aplicacion despues de una pausa, ademas de inicializar los recursos necesarios para el correcto funcionamiento de la aplicacion
     * @author manu
     * @since 1.0
     * */
    @Override
    public void onResume() {

        super.onResume();
        videoElegido = getIntent().getIntExtra("videoElegido", 0);
        mute = getIntent().getIntExtra("mute", 0);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //Inicializar el dibujo de la cara animada
        //imagencara = findViewById(R.id.cara);
        //imagencara.setImageResource(R.mipmap.cara);

        //Se carga la liberia de OpenCV
        Log.i(TAG, "Cargando libreria .so incluida en proyecto...");
        if (!OpenCVLoader.initDebug()) { //Si no existe una arquitectura apta, carga con OpenCV Manager, si existe la carga de los archivos de la app

            Log.i(TAG, "Cargando libreria con OpenCV Manager...");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, ocvLoaderCallback);

        } else {
            ocvLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS); //Se carga correctamente porque existe en el proyecto la arquitectura del telefono
        }
    }

    /** Este metodo se encarga de desactivar la camara y de pausar la aplicacion una vez que el usuario sale de la misma
     * @author manu
     * @since 1.0
     * */
    @Override
    public void onPause() {
        super.onPause();

        //mCamara.stop();
        //mAparecer.stop();

        if (camara != null)
            camara.disableView(); //Deshabilita la camara

    }

    /** Este metodo se encarga de desactivar la camara y destruir la actividad actual para asi liberar memoria
     * @author manu
     * @since 1.0
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();

        //mCamara.stop();

        if (camara != null)
            camara.disableView();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert cameraBridgeViewBasee != null;
            cameraBridgeViewBasee.releasePointerCapture();
        }*/
    }

    /** Este metodo se encarga de inicializar los recursos necesarios para que la camara funcione correctamente
     * @author manu
     * @since 1.0
     * @param width  Contiene la proporcion (ancho) de la imagen captada por la camara
     * @param height  Contiene la proporcion (alto) de la imagen captada por la camara
     * */
    @Override
    public void onCameraViewStarted(int width, int height) {
        gray = new Mat();
    }

    /** Este metodo se encarga de liberar los recursos que estan siendo consumidos por la camara
     * @author manu
     * @since 1.0
     * */
    @Override
    public void onCameraViewStopped() {
        gray.release();

    }

    /** Este metodo se encarga de capturar y mostrar lo que capta la camara del dispositivo, frame a frame, ademas de realizar las detecciones de la cara y la emocion
     * @author manu
     * @since 1.0
     * @param src  Objeto de tipo Mat que almacena cada frame de la camra
     * @return Mat  Devuelve un objeto de tipo Mat con cada frame detectado
     * */
    @Override
    public Mat onCameraFrame(Mat src) {

        Mat srcClone=src.clone(); //Se copia el frame que se esta detectando en un objeto de tipo Mat
        Imgproc.cvtColor(srcClone, srcClone, Imgproc.COLOR_BGR2RGB);    //Cambiar de BGR 3 canales: color (lo mismo que RGB) a Gris  1 canal
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);    //Cambiar de BGR 3 canales: color a Gris  1 canal
        Imgproc.equalizeHist(gray, gray);                       //Eq histograma

        MatOfRect faces = new MatOfRect();      //objeto de tipo MatOfRect que almacenara las caras detectadas (Matriz)
        if (cara != null && !cara.empty()) {    //Control de errores
            cara.detectMultiScale(gray, faces, 1.1, 7, 0, new Size(180, 350), new Size());    //Con el modelo cargado se aplica el clasificador para detectar las caras en tiempo real //ENTRE 100 y 80 esta bien

        }

        //Itera para cada cara (lista)
        for (Rect rc : faces.toList()) {
            Log.i(TAG, "Cara detectada");
            Imgproc.rectangle(src, rc.tl(), rc.br(), new Scalar(0, 255, 0), 7);  //Dibujamos un rectangulo para cada una de las caras detectadas
        }

        // Itera para cada cara como el bucle de arriba (vector)
        for (int i = 0; i < faces.toArray().length; i++) {
            Rect faceRect = faces.toArray()[i];  //Almacena las caras en un objeto de tipo Rect (vector)

            Mat sub_gray = src.submat(faceRect); //Objeto que almacena solo el tamaño justo de la cara detectada(es decir el rectangulo)

            MatOfRect sDetections = new MatOfRect();  //Para la sonrisa
            MatOfRect surDetections = new MatOfRect();      //Para la sorpresa

            Imgproc.cvtColor(src.submat(faceRect), sub_gray, Imgproc.COLOR_RGB2GRAY); //Pasamos tambien la submatriz a gris
            Imgproc.equalizeHist(sub_gray, sub_gray); //Eq histograma

            if (smile != null && !smile.empty()) { //Control de errores
                smile.detectMultiScale(sub_gray, sDetections, 1.25, 65, 0,
                        new Size(110, 100), new Size()); //Con el modelo cargado se aplica el clasificador para detectar la sonrisa en tiempo real
            }

            //min 69
            if (surprise != null && !surprise.empty()) {
                surprise.detectMultiScale(sub_gray, surDetections, 1.26, 145, 0,
                        new Size(70, 70), new Size(150, 150)); //Con el modelo cargado se aplica el clasificador para detectar la sorpresa en tiempo real
            }
            //min 189
            if (videoElegido == 1) {
                // Itera para la cantidad de sonrisas que se han encontrado
                for (int y = 0; y < sDetections.toArray().length; y++) {

                    /*Rect sRect = sDetections.toArray()[y];

                    // Proporciona la posicion relativa de las sonrisas con respecto a la cara
                    Point center = new Point(faceRect.x + sRect.x + sRect.width / 2,
                            faceRect.y + sRect.y + sRect.height / 2);
                    // Dibuja una elipse para cada sonrisa detectada de color morado
                    Imgproc.ellipse(src, center, new Size(sRect.width / 2, sRect.height / 2),
                            0, 0, 360, new Scalar(255, 0, 255), 2, 0, 0);*/


                    //Corrección para la v2
                    //El hilo principal (que es el que puede modificar las vista en tiempo real) cambia la cara dibujo de la expresion
                    /*
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            imagencara = findViewById(R.id.cara);
                            imagencara.setImageResource(R.mipmap.cara_alegria);

                        }
                    });
*/

                    if(mute == 0) musicCamara(); //Sonido de captura de camara
                    Log.i(TAG, "Sonrisa detectada");

                    //Crea carpeta dentro del dispositivo donde se almacenara las capturas tomadas
                    FolderUtil.createDefaultFolder("/storage/emulated/0/Detector");
                    //Path de las fotos que se tomaran
                    String outPicture = ImageUtils.SCAN_IMAGE_LOCATION + File.separator + "smile" + ImageUtils.generateFilename();
                    String outPicture_mostrar = ImageUtils.SCAN_IMAGE_LOCATION + File.separator + "smile_mostrar" + ImageUtils.generateFilename();

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Se captura tanto la imagen a mostrar al usuario como la imagen "crop" de la cara y se guardan en un objeto de tipo Mat
                    Imgcodecs.imwrite(outPicture, sub_gray);
                    Imgcodecs.imwrite(outPicture_mostrar, srcClone);
                    Log.d(TAG, "Path " + outPicture);

                    Intent cvIntent = new Intent(this, MainActivity.class); //Se le devuelve el control al Main
                    cvIntent.putExtra("var", 1); //Se le pasa una variable para controlar la siguiente actividad
                    cvIntent.putExtra("exp", 1); //Se le pasa una variable con la expresion que se encontro
                    cvIntent.putExtra("foto", outPicture); //Se le pasa el path de la foto "crop" de la cual se realizara la clasificacion
                    cvIntent.putExtra("foto2", outPicture_mostrar); //Se le pasa el path de la captura tomada para mostrarsela al usuario
                    startActivity(cvIntent);

                }
            } else if (videoElegido == 2) {
                //Ocurre lo mismo que con la sonrisa(esto se realiza para cada frame)
                for (int y = 0; y < surDetections.toArray().length; y++) {

                    /*Rect surRect = surDetections.toArray()[y];

                    Point center2 = new Point(faceRect.x + surRect.x + surRect.width / 2,
                            faceRect.y + surRect.y + surRect.height / 2);
                    Imgproc.ellipse(src, center2, new Size(surRect.width / 2, surRect.height / 2),
                            0, 0, 360, new Scalar(0, 0, 255), 2, 0, 0);*/


                    //Corrección para la v2
                    /*

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            imagencara = findViewById(R.id.cara);
                            imagencara.setImageResource(R.mipmap.cara_asombro);

                        }
                    });

                    */

                    if(mute == 0) musicCamara();
                    Log.i(TAG, "Sorpresa detectada");

                    FolderUtil.createDefaultFolder("/storage/emulated/0/Detector");
                    String outPicture = ImageUtils.SCAN_IMAGE_LOCATION + File.separator + "surprise" + ImageUtils.generateFilename();
                    String outPicture_mostrar = ImageUtils.SCAN_IMAGE_LOCATION + File.separator + "surprise_mostrar" + ImageUtils.generateFilename();

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Imgcodecs.imwrite(outPicture, sub_gray);
                    Imgcodecs.imwrite(outPicture_mostrar, srcClone);

                    Log.d(TAG, "Path " + outPicture);


                    Intent cvIntent = new Intent(this, MainActivity.class);
                    cvIntent.putExtra("var", 1);
                    cvIntent.putExtra("exp", 2);
                    cvIntent.putExtra("foto", outPicture);
                    cvIntent.putExtra("foto2", outPicture_mostrar);
                    startActivity(cvIntent);

                }
            }
        }

            return src;
        }

    /** Este metodo se encarga de controlar el sonido de captura de la imagen
     * @author manu
     * @since 1.0
     * */

    public void musicCamara(){

        this.mCamara = MediaPlayer.create(this, R.raw.disparocamara);
        this.mCamara.seekTo(0);
        this.mCamara.start();
        mCamara.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mCamara) {
                mCamara.reset();

            }
        });

    }

    /** Esta funcion se encarga de la voz para poner la expresion correspondiente
     * @author manu
     * @since 1.0
     * */

    public void vozPonerExpresion(int videoElegido){

        if(videoElegido==1) {
            mPonerCara = MediaPlayer.create(this, R.raw.poneralegria);

                    imagencara = findViewById(R.id.cara);
                    imagencara.setImageResource(R.mipmap.cara_alegria);

        }
        else if(videoElegido==2){

            mPonerCara = MediaPlayer.create(this, R.raw.ponersorpresa);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    imagencara = findViewById(R.id.cara);
                    imagencara.setImageResource(R.mipmap.cara_asombro);

                }
            });
        }

        mPonerCara.seekTo(0);
        mPonerCara.start();
        mPonerCara.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mPonerCara) {
                mPonerCara.reset();

            }
        });

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

    /** Esta funcion se encarga de la musica al pulsar un boton de la actividad
     * @author manu
     * @since 1.0
     * */
    public void bottonmusic(){

        this.mBoton = MediaPlayer.create(this, R.raw.pulsar_boton);
        this.mBoton.seekTo(0);
        this.mBoton.start();
        mBoton.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mBoton) {
                mBoton.release();

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

}

