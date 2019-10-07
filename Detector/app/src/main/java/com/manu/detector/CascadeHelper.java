package com.manu.detector;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


/** Actividad de apoyo a obtener los modelos de OpenCV
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class CascadeHelper {

    /** Esta funcion se encarga de obtener el path a los modelos de OpenCV
     * @author manu
     * @since 1.0
     * @param ctx  Objeto que almacena el contexto de la actividad
     * @param id  Variable entera que almacena el nombre del recurso a cargar
     * @param diff  Variable entera de control para saber que XML se esta solicitando
     * @return String  Devuelve el path del modelo XML
     * */

    public static String generateXmlPath(Context ctx, int id, int diff) {
        try {

            InputStream is = ctx.getResources().openRawResource(id);
            File mCascadeFile;
            FileOutputStream os;

            if(diff==1) {   //Carga el XML correspondiente a la deteccion de la cara
                File cascadeDir = ctx.getDir("haarcascade_frontalface", Context.MODE_PRIVATE);
                mCascadeFile = new File(cascadeDir, "haarcascade_frontalface.xml");
                os = new FileOutputStream(mCascadeFile);

            }else if(diff==2){ //Carga el XML correspondiente a la deteccion de la sonrisa

                File cascadeDir = ctx.getDir("haarcascade_smile", Context.MODE_PRIVATE);
                mCascadeFile = new File(cascadeDir, "haarcascade_smile.xml");
                os = new FileOutputStream(mCascadeFile);

            }else{  //Carga el XML correspondiente a la deteccion de la sorpresa

                File cascadeDir = ctx.getDir("haarcascade_surprise", Context.MODE_PRIVATE);
                mCascadeFile = new File(cascadeDir, "haarcascade_surprise.xml");
                os = new FileOutputStream(mCascadeFile);

            }


            byte[] buffer = new byte[4096];
            int bytesRead = 0;

            //Lee el fichero de entrada y escribe en el de salida, es necesario para la primera vez que se ejecuta la actividad
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();

            return mCascadeFile.getAbsolutePath();

        } catch (Exception e) {
            return null;
        }
    }
}
