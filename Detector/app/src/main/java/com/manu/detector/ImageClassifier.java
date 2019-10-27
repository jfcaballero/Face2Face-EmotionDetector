/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.manu.detector;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

//Transparente al usuario, se encarga de procesar la imagen

/** Classifica la imagen a partir del modelo de TensorFlow Lite
 * @author manu
 * @version 1.0
 * @since 1.0
 * */

public class ImageClassifier {

  /** Tag de la clase. {@link Log}. */
  public static final String TAG = "ImageClassifier";

  /** Variable que almacena el nombre del modelo almacenado en la carpeta assets */
  public static final String MODEL_PATH = "graph.lite";

  /** Variable que almacena el nombre del archivo con las etiquetas almacenado en la carpeta assets */
  public static final String LABEL_PATH = "labels.txt";

  /** Numero de resultados mostrados en la interfaz, solo para el programador */
  public static final int RESULTS_TO_SHOW = 1;

  /** Variable que almacena la dimension de las entradas */
  public static final int DIM_BATCH_SIZE = 1;
  public static final int DIM_PIXEL_SIZE = 3;

  /** Variable que almacena el tamaño en anchura de las imagenes de entrada */
  static final int DIM_IMG_SIZE_X = 224;
  /** Variable que almacena el tamaño de altura de las imagenes de entrada */
  static final int DIM_IMG_SIZE_Y = 224;

  /** Mas variables para almacenar la dimension de las imagenes de entradas */
  public static final int IMAGE_MEAN = 0;
  public static final float IMAGE_STD = 270;

  /** Variable para almacenar el resultado de la clasificacion, solo para el programador */
  public String[] textToShow = new String[2];

  /* Buferes preasignados para almacenar datos de la imagen */
  public int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

  /** Un objeto de tipo Interpreter para ejecutar la inferencia del modelo con Tensorflow Lite. */
  public Interpreter tflite;

  /** Lista de etiquetas correspondientes a los resultados del modelo, para almacenar uno o mas de un resultado */
  public List<String> labelList;

  /** Un objeto de tipo ByteBuffer para guardar los datos de imagen, para alimentar las entradas del modelo de Tensorflow Lite */
  public ByteBuffer imgData = null;

  /** Un vector para guardar los resultados de la inferencia, para alimentar las salidas del modelo de Tensorflow Lite. */
  public float[][] labelProbArray = null;
  /** Filtro de paso bajo multietapa **/
  public float[][] filterLabelProbArray = null;
  /** Etapas del filtro de paso bajo multietapa **/
  public static final int FILTER_STAGES = 3;
  /** Factor del filtro de paso bajo multietapa **/
  public static final float FILTER_FACTOR = 0.4f;

  /** Objeto de tipo cola que compara la confianza de los resultados y los ordena de mayor a menor **/
  public PriorityQueue<Map.Entry<String, Float>> sortedLabels =
      new PriorityQueue<>(
          RESULTS_TO_SHOW,
          new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
              return (o1.getValue()).compareTo(o2.getValue()); //Compara la confianza de los resultados y los ordena de mayor a menor (Colas)
            }
          });


  /** Esta funcion se encarga de crear e inicializar un {@code ImageClassifier}
   * @author manu
   * @since 1.0
   * @param activity Objeto de tipo Activity a traves del cual se conecta la clase que hace la clasificacion con la que muestra los resultados
   * @return ImageClaaifier  Devuelve un objeto de la clase
   * */
  ImageClassifier(Activity activity) throws IOException {
    tflite = new Interpreter(loadModelFile(activity)); //Se carga el modelo
    labelList = loadLabelList(activity); //Se carga las etiquetas
    imgData =
        ByteBuffer.allocateDirect(
            4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE); //Objeto ByteBuffer que almacenara los datos de las imagenes
    imgData.order(ByteOrder.nativeOrder());
    labelProbArray = new float[1][labelList.size()];  //Guarda los resultados de la inferencia
    filterLabelProbArray = new float[FILTER_STAGES][labelList.size()]; //Guarda los resultados del filtro de paso bajo multietapa
    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
  }

  /** Esta funcion se encarga de clasificar la imagen
   * @author manu
   * @since 1.0
   * @param bitmap  Imagen de tipo Bitmap sobre la cual de realizara la clasificacion
   * @return textToShow  Un vector de String con los resultados de la clasificacion
   * */
  public String[] classify(Bitmap bitmap) {
    if (tflite == null) {
      Log.e(TAG, "Image classifier has not been initialized; Skipped.");
      System.exit(1);
    }
    convertBitmapToByteBuffer(bitmap);
        //Se inicia un reloj que controla el tiempo que tarda ne hacer la clasificacion
        long startTime = SystemClock.uptimeMillis();
        tflite.run(imgData, labelProbArray); //Se inicia la clasificacion
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to run model inference: " + (endTime - startTime));

        // suaviza los resultados
        applyFilter();

        // print the results
        //String[] textToShow = new String[2];

        //Escribe en textToShow el valor top de las etiquetas
        textToShow = printTopKLabels();
        //textToShow = Long.toString(endTime - startTime) + "ms" + textToShow;

    return textToShow;
  }

  /** Esta funcion se encarga de aplicar el filtro de paso bajo multietapa
   * @author manu
   * @since 1.0
   * */

  public void applyFilter(){
    int num_labels =  labelList.size();

    // Low pass filter `labelProbArray` into the first stage of the filter.
    //Aplica un filtro de paso bajo en la primera epoca
    for(int j=0; j<num_labels; ++j){
      filterLabelProbArray[0][j] += FILTER_FACTOR*(labelProbArray[0][j] -
                                                   filterLabelProbArray[0][j]);
    }
    //Aplica un filtro de paso bajo en las siguientes epocas sin contar la primera
    for (int i=1; i<FILTER_STAGES; ++i){
      for(int j=0; j<num_labels; ++j){
        filterLabelProbArray[i][j] += FILTER_FACTOR*(
                filterLabelProbArray[i-1][j] -
                filterLabelProbArray[i][j]);

      }
    }

    //Copia la ultima epoca del filtro de salida a labelProbArray
    for(int j=0; j<num_labels; ++j){
      labelProbArray[0][j] = filterLabelProbArray[FILTER_STAGES-1][j];
    }
  }

  /** Esta funcion se encarga de liberar los recursos consumidos por el clasificador
   * @author manu
   * @since 1.0
   * */
  public void close() {
    tflite.close();
    tflite = null;
  }

  /** Esta funcion se encarga de leer la lista con las etiquetas
   * @author manu
   * @since 1.0
   * @param activity  Objeto de tipo Activity a traves del cual se conecta la clase que hace la clasificacion con la que muestra los resultados
   * @return List <string>  Devuelve una lista con las etiquetas
   * */
  public List<String> loadLabelList(Activity activity) throws IOException {
    List<String> labelList = new ArrayList<String>();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(activity.getAssets().open(LABEL_PATH)));
    String line;
    while ((line = reader.readLine()) != null) {
      labelList.add(line);
    }
    reader.close();
    return labelList;
  }

  /** Esta funcion se encarga de mapear en memoria el archivo del modelo y de cargarlo, alojado en la carpeta assets
   * @author manu
   * @since 1.0
   * @param activity  Objeto de tipo Activity a traves del cual se conecta la clase que hace la clasificacion con la que muestra los resultados
   * @return MappedByteBuffer  Devuelve un objeto MappedByteBuffer con el modelo
   * */
  public MappedByteBuffer loadModelFile(Activity activity) throws IOException {
    AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  /** Esta funcion se encarga de escribir los datos de la imagen, tras convertirlos de un {@code Bitmap} a un {@code ByteBuffer}
   * @author manu
   * @since 1.0
   * @param bitmap  Imagen de entrada en formato {@code Bitmap}
   * */
  public void convertBitmapToByteBuffer(Bitmap bitmap) {
    if (imgData == null) {
      return;
    }
    imgData.rewind();
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight()); //Toma los pixeles de la imagen bpm
    // Convert the image to floating point.
    int pixel = 0;
    long startTime = SystemClock.uptimeMillis();
    for (int i = 0; i < DIM_IMG_SIZE_X; ++i) { //Para cada pixel
      for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) { //De la imagen
        final int val = intValues[pixel++];
        imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD); //Debemos proporcionarle al modelo un numero de coma flotante para cada canal donde el valor este entre 0 y 1. Para ello, enmascaramos cada canal de color como antes, pero luego dividimos cada valor resultante por 255.f.
        imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
        imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
      }
    }
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Timecost to put values into ByteBuffer: " + (endTime - startTime));
  }

  /** Esta funcion se encarga de almacenar el resultado de la clasificacion en una variable de tipo String para posteriormente devolverla
   * @author manu
   * @since 1.0
   * @return String[]  Vector de {@code String} con los resultados de la clasificacion
   * */
  public String[] printTopKLabels() {
    for (int i = 0; i < labelList.size(); ++i) { //Itera para cada etiqueta
      sortedLabels.add(
          new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i])); //las compara y las añade y las ordena en una cola
      if (sortedLabels.size() > RESULTS_TO_SHOW) { //Para mostrar mas de 1 resultado
        sortedLabels.poll(); //Recupera y elimina la cabeza de la cola, si no hay nada devuelve null
      }
    }
    //String textToShow = "";
      String[] textToShow2 = new String[2]; //Almacena la etiqueta y la confianza
    //final int size = sortedLabels.size();
    //for (int i = 0; i < size; ++i) {
      Map.Entry<String, Float> label = sortedLabels.poll(); //Recupera y elimina la cabeza de la cola
      textToShow2[0]=label.getKey(); //Aqui va la etiqueta
      textToShow2[1]= String.valueOf(label.getValue()); //Y aqui la confianza
      //textToShow = String.format("\n%s: %4.2f",label.getKey(),label.getValue()) + textToShow;
    //}
    return textToShow2;
  }
}
