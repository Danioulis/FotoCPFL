package com.widowsoftware.daniel.fotocpfl;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_FOTO = "com.widowsoftware.FotoCPFL.FOTO";
    public final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 150;
    Camera camera;
    private ViewCamera mPreview;
    Button tirarFoto;
    FrameLayout fullFotoView;
    ViewTreeObserver vto;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tirarFoto = (Button)findViewById(R.id.button_image);
        fullFotoView = (FrameLayout)findViewById(R.id.comBorda);
        vto  =  fullFotoView.getViewTreeObserver();
        tirarFoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //fully drawn, no need of listener anymore
                //getDrawingBitmap();
                camera.takePicture(null,null,mPicture);
            }
        });

        if(checkCameraHardware(this) && checkFilePermission(this)){
            camera = getCameraInstance();
        }
        mPreview = new ViewCamera(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.previewCamera);
        preview.addView(mPreview);
    }

    public static Bitmap viewToBitmap(View view, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2,new Rect(0,0,bmp2.getWidth(),bmp2.getHeight()), new Rect(807,80, 2156,1423), null);
        return bmOverlay;
    }

    public void getDrawingBitmap(File destination, String filename){
        Drawable fotoPre = Drawable.createFromPath(destination.getPath());
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.borda);

        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());
        //String filename = "Foto: " + format +".png";
        //File directory = Environment.getExternalStorageDirectory();
        //File destination = new File(directory, filename);

        //Bitmap b = viewToBitmap(fullFotoView,fullFotoView.getWidth(), fullFotoView.getHeight());
        Bitmap c = ((BitmapDrawable) fotoPre).getBitmap();
        Bitmap completo = overlay(b, c);
        b.recycle();
        b = null;
        c.recycle();
        c = null;
        Intent intent = new Intent(this, EndActivity.class);
        intent.putExtra(EXTRA_FOTO, filename);

        FileOutputStream out = null;
        try {
            FileOutputStream outputFoto = new FileOutputStream(destination);
            completo.compress(Bitmap.CompressFormat.PNG, 90, outputFoto);
            outputFoto.flush();
            outputFoto.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        completo.recycle();
        completo = null;
        System.gc();
        Runtime.getRuntime().totalMemory();
        Runtime.getRuntime().freeMemory();

        startActivity(intent);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
            String format = s.format(new Date());
            String filename = "Foto: " + format +".png";
            File directory = Environment.getExternalStorageDirectory();
            File destination = new File(directory, filename);
            if (destination == null){
                Log.d("Error", "Error creating media file, check storage permissions.");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(destination);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Error: ", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Error: ", "Error accessing file: " + e.getMessage());
            }

            camera.stopPreview();
            camera.release();
            camera = null;
            getDrawingBitmap(destination, filename);


        }
    };



    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
        if (camera != null) {
            camera.release();
            FrameLayout preview = (FrameLayout) findViewById(R.id.previewCamera);
            preview.removeView(mPreview);
            camera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Get the Camera instance as the activity achieves full user focus

        if (camera == null) {
            camera = getCameraInstance();
            mPreview = new ViewCamera(this, camera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.previewCamera);
            preview.addView(mPreview);
            camera.startPreview();
        }
    }

    public static Camera getCameraInstance(){
        int cameraCount = 0;
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    c = Camera.open(camIdx);
                } catch (RuntimeException e) {
                }
            }
        }
        return c;
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private boolean checkFilePermission(Context context) {
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE  );

        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE  );

        return permissionCheck == PackageManager.PERMISSION_GRANTED;

    }

}
