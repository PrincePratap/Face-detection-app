package com.facedetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView textView;
    ImageView imageView;

    private  final  static  int REQUEST_IMAGE_CAPTURE = 123;
    InputImage firebaseVision;
    FaceDetection visionFaceDetection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        textView = findViewById(R.id.Text123);
        imageView = findViewById(R.id.imageView);

        FirebaseApp.initializeApp(this);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenIntent();
            }
        });
    }

    private void OpenIntent() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!= null){
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
        }else {
            Toast.makeText(this, "intent fail ", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap) bundle.get("data");
        FaceDetectionProcess(bitmap);
        Toast.makeText(this, "face detection app", Toast.LENGTH_SHORT).show();





    }

    private void FaceDetectionProcess(Bitmap bitmap) {
        textView.setText("processing  image");

        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking().build();

        FaceDetector faceDetector =  FaceDetection.getClient(highAccuracyOpts);
        Task<List<Face>> task = faceDetector.process(image);

        task.addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {


                if(faces.size() != 0){
                    if (faces.size() == 1 ){

                        builder.append(faces.size()+"face Detected \n \n");
                    }else if(faces.size()>1){
                        builder.append(faces.size()+"face Detected");
                    }

                }


                for(Face face : faces){

                    int id = face.getTrackingId();
                    float rotY = face.getHeadEulerAngleY();
                    float rotX = face.getHeadEulerAngleX();

                    builder.append(" 1 face tracking id ["+id+"]\n");
                    builder.append("\n 2 head Rotation  to Right ["+String.format("% 2f",rotY));
                    builder.append("\n 3 head Rotation  to lift ["+String.format("% 2f",rotX));


                    //smiling
                    if(face.getSmilingProbability()>0){
                        float smilingProbability = face.getSmilingProbability();
                        builder.append("\n 4 smiling probability["+String.format("% 2f",smilingProbability)+"]\n");
                    }
                    //left eye opened

                    if(face.getLeftEyeOpenProbability()>0){

                        float leftEye = face.getLeftEyeOpenProbability();
                        builder.append("\n5 left eye  probability["+String.format("% 2f",leftEye)+"]\n");
                    }

                    //right eye opened
                    if(face.getRightEyeOpenProbability()>0){
                        float rightEye = face.getRightEyeOpenProbability();
                        builder.append(" \n6 right  eye  probability["+String.format("% 2f",rightEye)+"]\n");

                    }
                    builder.append("\n");

                }


                ShowDetection("face detection",builder, true);



            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                final StringBuilder builder1 = new StringBuilder();
                builder1.append("sorry bro ");
                ShowDetection("face detection",builder1,false);


            }
        });






    }

    private void ShowDetection(String title, StringBuilder builder, boolean success) {

        if(success==true){
            textView.setText(null);
            textView.setMovementMethod(new ScrollingMovementMethod());




            if(builder.length()!= 0){
                textView.append(builder);
                 if(title.substring(0,title.indexOf(' ')).equalsIgnoreCase("OCR")){
                  textView.append("\nhold the text for copy it ");
                 }else{
                    textView.append(" hold the text for copy it ???????");
                  }


                     textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText(title,builder);
                    clipboardManager.setPrimaryClip(clipData);
                    return  true;

                    }
                });

            }else{
                textView.append(title.substring(0,title.indexOf(' '))+ " fail to find anything ");
            }
        } else if(success==false){
            textView.setText(null);
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.append(builder);

        }
    }
}
