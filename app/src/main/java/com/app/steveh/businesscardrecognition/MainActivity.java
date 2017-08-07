package com.app.steveh.businesscardrecognition;

import android.Manifest;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.hardware.Camera;
import android.os.Build;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private Button takePictureButton, resetButton;
    private TextView textBlockContent;

    private Camera mCamera;
    private CameraPreview mPreview;

    private FrameLayout preview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        textBlockContent = (TextView) findViewById(R.id.textBlockContent);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        takePictureButton = (Button) findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reset();
            }
        });;
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {


        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
            getText(bitmap);
        }
    };

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    private void reset() {
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        preview.removeAllViews();
        preview.addView(mPreview);
        textBlockContent.setText("");
    }

    private void getText(Bitmap image) {

        AssetManager assetManager = getAssets();
        InputStream istr;
        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();

        try {
            istr = assetManager.open("card1.png");
            Bitmap bitmap = BitmapFactory.decodeStream(istr);
            istr.close();
            if (!textRecognizer.isOperational()) {
                new AlertDialog.Builder(this)
                        .setMessage("Text recognizer could not be set up on your device :(").show();
                return;
            }
            Frame frame = new Frame.Builder().setBitmap(image).build(); //TODO change "bitmap" to "image" to use the picture taken by the camera
            SparseArray<TextBlock> text = textRecognizer.detect(frame);
            String detectedText = "";

            for (int i = 0; i < text.size(); i++) {
                TextBlock textBlock = text.valueAt(i);
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText += textBlock.getValue();
                }
                textBlockContent.setText(detectedText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        int n = 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }




}
