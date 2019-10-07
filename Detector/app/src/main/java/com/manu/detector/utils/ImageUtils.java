package com.manu.detector.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

//Transparente al usuario.

/** Actividad de soporte, encargada del preprocesamiento de la imagen
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class ImageUtils {

    /** Variable que almacena el path de la carpeta donde se guardan las imagenes */
    public static final String SCAN_IMAGE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "Detector";

    /** Esta actividad se encarga de redimensionar la imagen bitmap para que concuerde con la del modelo
     * @author manu
     * @since 1.0
     * @param b - Imagen de entrada que será preprocesada
     * @param newWidth - Variable que almacena el nuevo ancho de la imagen
     * @param newHeight - Variable que almacena el nuevo alto de la imagen
     * */
    public static Bitmap getResizedBitmap(Bitmap b, int newWidth, int newHeight) {
        int width = b.getWidth();
        int height = b.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // Crea una matriz para la manipulación
        Matrix matrix = new Matrix();
        // Redimensiona el BitMap
        matrix.postScale(scaleWidth, scaleHeight);

        // Recrea el Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(b, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    /** Esta actividad se encarga de generar un nombre distintivo para la imagen
     * @author manu
     * @since 1.0
     * */
    public static String generateFilename(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return sdf.format(new Date()) + ".jpg";
    }

    /** Esta función se encarga de eliminar las imágenes que se almacenan en el móvil tras la clasificación
     * @author manu
     * @since 1.0
     * @param foto - String que almacena la ruta de la imagen a clasificar
     * @param foto2 - String que almacena la ruta de la imagen a mostrar
     * */
    public static void eliminarArchivos(String foto, String foto2){


        if(FolderUtil.checkIfFileExist(foto)){ //Si la imagen existe
            new File(foto).delete(); //La borra para no consumir memoria del dispositivo
        }
        if(FolderUtil.checkIfFileExist(foto2)) {
            new File(foto2).delete();
        }

    }

}
