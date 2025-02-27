package com.arcedios.myapp2.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.arcedios.myapp2.Model.Classifier;
import com.arcedios.myapp2.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.speech.tts.TextToSpeech;
import java.util.Locale;


public class Servicios extends AppCompatActivity {

    private androidx.camera.view.PreviewView camara;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Button recognizeButton;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private TextView resultTextView, OjosAbiertos, OjosCerrados, NoHayNadie;

    private Handler handler = new Handler();
    private boolean isAnalyzing = false; // Para controlar el estado del análisis
    private ArrayList<String> historialResultados = new ArrayList<>();
    private boolean isColorChanging = false; // Controla si el fondo está cambiando
    private Handler colorHandler = new Handler();
    private Runnable colorRunnable;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                //textToSpeech.setLanguage(Locale.US); // Idioma en inglés
                textToSpeech.setLanguage(new Locale("es", "US")); // Para español latino
            }
        });

        String userId = getIntent().getStringExtra("USER_ID");

        camara = findViewById(R.id.camara);
        recognizeButton = findViewById(R.id.button2);
        resultTextView = findViewById(R.id.Resultado);
        OjosAbiertos = findViewById(R.id.ojosAbiertos2);
        OjosCerrados = findViewById(R.id.ojosCerrados2);
        NoHayNadie = findViewById(R.id.noHayNadie2);



        cameraExecutor = Executors.newSingleThreadExecutor();

        // Iniciar análisis al presionar el botón
        recognizeButton.setOnClickListener(v -> toggleAnalysis());
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Verifica permisos y arranca la cámara cada vez que la actividad se reanuda
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(camara.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageCapture);
            } catch (Exception e) {
                Log.e("CameraX", "Error al iniciar la cámara", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario concede el permiso, inicia la cámara
                startCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void toggleAnalysis() {
        if (isAnalyzing) {
            isAnalyzing = false;
            handler.removeCallbacksAndMessages(null); // Detener análisis
            Toast.makeText(this, "Análisis detenido", Toast.LENGTH_SHORT).show();
        } else {
            isAnalyzing = true;
            startAnalysisLoop();
            Toast.makeText(this, "Análisis iniciado", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAnalysisLoop() {
        if (!isAnalyzing) return;

        takePhoto();

        // Repetir cada 5 segundos
        handler.postDelayed(this::startAnalysisLoop, 2000);
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captured_image.jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                processImage(photoFile);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(Servicios.this, "Error al capturar imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void processImage(File imageFile) {
        try {
            Bitmap correctedBitmap = fixImageRotation(imageFile.getAbsolutePath());
            if (correctedBitmap == null) return;

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(correctedBitmap, 224, 224, true);
            saveProcessedImage(resizedBitmap);

            float[][][][] input = preprocessImage(resizedBitmap);

            Classifier classifier = new Classifier(getAssets(), "model.tflite");
            float[][] results = classifier.predict(input);
            classifier.close();



            OjosAbiertos.setText((String.valueOf(results[0][0] * 100) + "%"));
            OjosCerrados.setText((String.valueOf(results[0][1] * 100) + "%"));
            NoHayNadie.setText((String.valueOf(results[0][2] * 100) + "%"));

            String[] labels = {"Ojos abiertos", "Ojos cerrados", "No hay nadie"};
            int bestMatch = -1;
            float bestConfidence = 0;

            for (int i = 0; i < results[0].length; i++) {
                if (results[0][i] > bestConfidence) {
                    bestConfidence = results[0][i];
                    bestMatch = i;
                }
            }

            // Asegurar que solo haya 10 elementos en la lista
            if (historialResultados.size() == 10) {
                historialResultados.remove(0); // Eliminar el más antiguo
            }
            historialResultados.add(labels[bestMatch]); // Agregar el nuevo resultado

            resultTextView.setText(labels[bestMatch]);

            // Mostrar el historial en la consola
            for (String resultado : historialResultados) {
                Log.d("Historial", resultado);
            }

            // Llamar a analizarDatos() cuando hay 10 resultados
            if (historialResultados.size() == 10) {
                analizarDatos();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analiza los últimos 10 resultados y verifica si 5 o más son "Ojos cerrados".
     */
    private void analizarDatos() {
        String cedula = getIntent().getStringExtra("USER_ID");

        int countOjosCerrados = 0;

        for (String resultado : historialResultados) {
            if (resultado.equals("Ojos cerrados")) {
                countOjosCerrados++;
            }
        }
        if (countOjosCerrados >= 5) {
            String nombre = getIntent().getStringExtra("NOMBRE");
            String mensaje =  nombre + " despierta";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            startColorAnimation(); // Iniciar cambio de color
            speakWarning(mensaje); // Decir la advertencia
        } else {
            stopColorAnimation(); // Detener si ya no hay peligro
        }
    }
    private void speakWarning(String message) {
        if (textToSpeech != null) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void startColorAnimation() {
        if (isColorChanging) return; // Evitar que se inicie varias veces

        isColorChanging = true;
        colorRunnable = new Runnable() {
            private boolean isRed = false;

            @Override
            public void run() {
                int color = isRed ? ContextCompat.getColor(Servicios.this, R.color.blue) :
                        ContextCompat.getColor(Servicios.this, R.color.red);
                findViewById(android.R.id.content).setBackgroundColor(color);
                isRed = !isRed;

                if (isColorChanging) {
                    colorHandler.postDelayed(this, 500); // Cambia cada 500ms (0.5 segundos)
                }
            }
        };

        colorHandler.post(colorRunnable);
    }

    private void stopColorAnimation() {
        isColorChanging = false;
        colorHandler.removeCallbacks(colorRunnable);
        findViewById(android.R.id.content).setBackgroundColor(ContextCompat.getColor(this, R.color.white)); // Volver a blanco
    }



    private void saveProcessedImage(Bitmap bitmap) {
        File processedImageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "processed_image.jpg");
        try (FileOutputStream out = new FileOutputStream(processedImageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            Log.e("SaveImage", "Error al guardar la imagen procesada", e);
        }
    }

    private float[][][][] preprocessImage(Bitmap bitmap) {
        int width = 224, height = 224;
        float[][][][] input = new float[1][width][height][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                input[0][y][x][0] = (((pixel >> 16) & 0xFF) / 127.5f) - 1;
                input[0][y][x][1] = (((pixel >> 8) & 0xFF) / 127.5f) - 1;
                input[0][y][x][2] = ((pixel & 0xFF) / 127.5f) - 1;
            }
        }
        return input;
    }
    private Bitmap fixImageRotation(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return BitmapFactory.decodeFile(imagePath);
            }

            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
            return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        cameraExecutor.shutdown();
        handler.removeCallbacksAndMessages(null);
    }

}
