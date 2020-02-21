package com.manu.detector.utils;

import java.io.File;

//Transparente al usuario.

/** Actividad soporte, contiene lo necesario para crear carpetas y comprobar previamente si existen
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class FolderUtil {

    /** Esta actividad se encarga de crea la carpeta para almacenar las imagenes
     * @author manu
     * @since 1.0
     * @param dirPath - Objeto de tipo String que almacena la ruta a la carpeta a crear
     * */
    public static void createDefaultFolder(String dirPath){
        File directory = new File(dirPath);
        if(!directory.exists()){
           directory.mkdir();
        }
    }

    //Comprueba que el archivo existe
    /** Esta actividad se encarga de comprobar que la carpeta o el archivo existe
     * @author manu
     * @since 1.0
     * @param filePath - Objeto de tipo String que almacena la ruta a la carpeta o archivo que queremos comprobar
     * */
    public static boolean checkIfFileExist(String filePath){
        File file = new File(filePath);
        return file.exists();
    }
}
