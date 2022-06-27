package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.controls.templates.ThumbnailTemplate;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView result,confidencesText,confidence;
    Button btn_thuvien;
    ImageView imageView;
    int imageSize = 224;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        btn_thuvien = findViewById(R.id.btn_thuvien);
        confidencesText =findViewById(R.id.confidencesText);

        btn_thuvien.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // Launch camera if we have permission
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
    }
    public void classifyImage(Bitmap bitmap){
//        try {
//            Best model = Best.newInstance(getApplicationContext());
//
//            // Creates inputs for reference.
//            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 640, 640, 3}, DataType.FLOAT32);
//            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
//            byteBuffer.order(ByteOrder.nativeOrder());
//
//            int [] intValues = new int[imageSize*imageSize];
//            image.getPixels(intValues, 0 , image.getWidth(), 0 , 0 , image.getWidth() , image.getHeight());
//
//            int pixel = 0;
//            for(int i=0;i<imageSize;i++){
//                for (int j = 0; j<imageSize ; j++){
//                    int val = intValues[pixel++];
//                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
//                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
//                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
//                }
//            }
//            inputFeature0.loadBuffer(byteBuffer);
//
//            // Runs model inference and gets result.
//            Best.Outputs outputs = model.process(inputFeature0);
//            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//            float[] confidences = outputFeature0.getFloatArray();
//            int maxPos = 0;
//            float maxConfidences = 0.0f;
//            for(int i=0 ; i<3; i++){
//                if(confidences[i] > maxConfidences){
//                    maxConfidences = confidences[i];
//                    maxPos = i;
//                }
//            }
//            Log.d("sss","maxPos = "+maxConfidences+" tai vi tri : "+maxPos);
//            String [] classes = {"other","vietinbank","vietcombank"};
//
//            result.setText(classes[maxPos]);
//
//            String s = "";
//            for(int i=0; i<classes.length; i++){
//                s += String.format("%s: %.1f%%\n",classes[i], confidences[i] * 100);
//            }
//            Log.d("test_s","s : "+s);
//            confidence.setText(s);
//
//            // Releases model resources if no longer used.
//            model.close();
//        } catch (IOException e) {
//            // TODO Handle the exception
//            Log.d("TAG","sss : "+e.getMessage());
//        }
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bitmap);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            int maxPos = 0;
            float maxConfidences = 0.0f;
            for(int i=0 ; i<3; i++){
                if(probability.get(i).getScore() > maxConfidences){
                    maxConfidences = probability.get(i).getScore();
                    maxPos = i;
                }
            }

            String [] classes = {"other","vietcombank","vietinbank"};
            result.setText(classes[maxPos]);
            String s = "";
            for(int i=0; i<classes.length; i++){
                s += String.format("%s: %.1f%%\n",classes[i], probability.get(i).getScore()* 100);
            }
            Log.d("test_s","s : "+s);
            confidence.setText(s);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image,dimension,dimension);
            imageView.setImageBitmap(image);

            image = Bitmap.createScaledBitmap(image,imageSize,imageSize, false);
            classifyImage(image);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}