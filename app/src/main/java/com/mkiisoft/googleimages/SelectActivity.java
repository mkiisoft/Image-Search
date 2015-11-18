package com.mkiisoft.googleimages;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mkiisoft.googleimages.utils.crop.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * Created by mariano on 11/10/15.
 */
public class SelectActivity extends AppCompatActivity {

    private ImageView mGallery;
    private ImageView mCamera;
    private ImageView mInternet;

    public static final String TEMP_PHOTO_FILE_NAME  = "temp_photo";
    public static final String TEMP_CAMERA_FILE_NAME = "temp_camera";
    private File   mFileTemp;
    private File   mFileTempCam;
    private String mImagePath;
    private String path;
    private String from;

    private static int MAX_LENGTH = 20;

    public  static final int REQUEST_CODE_GALLERY      = 0x1;
    public  static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public  static final int REQUEST_CODE_CROP_IMAGE   = 0x3;
    private static final int REQUEST_CODE_RESOLVE_ERR  = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);

        mGallery  = (ImageView) findViewById(R.id.btn_gallery);
        mCamera   = (ImageView) findViewById(R.id.btn_camera);
        mInternet = (ImageView) findViewById(R.id.btn_internet);

        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                openCamera();
                Intent intent = new Intent(SelectActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        mInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startCropImage() {

        Intent intent = new Intent(this, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mImagePath);
        intent.putExtra(CropImage.SCALE, true);

        intent.putExtra(CropImage.ASPECT_X, 1);
        intent.putExtra(CropImage.ASPECT_Y, 1);

        intent.putExtra(CropImage.OUTPUT_X, 200);
        intent.putExtra(CropImage.OUTPUT_Y, 200);

        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    private void openGallery() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLVE_ERR
                && resultCode == RESULT_OK) {

        }

        if (resultCode != RESULT_OK) {

            return;
        }

        Bitmap bitmap;

        switch (requestCode) {
            case REQUEST_CODE_CROP_IMAGE:

                path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {

                    return;
                }

                Intent intent = new Intent(this, ImageActivity.class);

                intent.putExtra("url", mImagePath);
                intent.putExtra("id", random());
                intent.putExtra("not_internet", "true");

                startActivity(intent);
                break;
            case REQUEST_CODE_GALLERY:
                try {
                    mFileTemp = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang", random() + ".jpg");
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            from = "gallery";
                            mImagePath = String.valueOf(mFileTemp);
                        }
                    });

                    startCropImage();

                } catch (Exception e) {

                    Log.e("Gallery", "Error while creating temp file", e);
                }
                break;
            case REQUEST_CODE_TAKE_PICTURE:
                try {
                    mFileTempCam = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang", random() + ".jpg");
                    Bitmap b = (Bitmap) data.getExtras().get("data");
                    try{
                        OutputStream os = new FileOutputStream(mFileTempCam);
                        b.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        b.recycle();
                    }catch (Exception e){
                        System.out.println("Error while creating temp file");
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            from = "camera";
                            mImagePath = String.valueOf(mFileTempCam);
                        }
                    });

                    startCropImage();

                } catch (Exception e) {

                    Log.e("Camera", "Error while creating temp file", e);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public void deleteFolder(){
        File dir = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang");

        if(dir.exists()){
            if (dir.isDirectory())
            {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++)
                {
                    new File(dir, children[i]).delete();
                }
            }
        }
    }

    protected String random() {
        String randomchar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 12) {
            int index = (int) (rnd.nextFloat() * randomchar.length());
            salt.append(randomchar.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
