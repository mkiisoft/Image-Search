package com.mkiisoft.googleimages;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mkiisoft.googleimages.utils.crop.CropImage;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by mariano on 11/8/15.
 */
public class ImageActivity extends AppCompatActivity {

    private ImageView mImageFull;
    private TextView  mButtonCancel;
    private TextView  mButtonCrop;
    private TextView  mFileSize;
    private String    mCopyright;
    private String    imagePath;
    private String    path;
    private File      dir;
    private File      file;
    private boolean   didFinish   = false;
    private boolean   isCopyright = false;

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    public static final int REQUEST_CODE_CROP_IMAGE   = 0x3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        final String url = extras.getString("url");
        final String id  = extras.getString("id");

        dir  = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang");
        file = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang", id + ".jpg");

        mCopyright    = getResources().getString(R.string.copyright);

        mImageFull    = (ImageView) findViewById(R.id.image_full);
        mFileSize     = (TextView) findViewById(R.id.file_size);
        mButtonCancel = (TextView) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonCrop = (TextView) findViewById(R.id.button_crop);
        mButtonCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(didFinish && !isCopyright){
                    startCropImage();
                } else if (isCopyright){
                    Toast.makeText(ImageActivity.this, mCopyright, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Glide.with(this).load(url).placeholder(R.drawable.ic_loading).into(new SimpleTarget() {
            @Override
            public void onResourceReady(Object resource, GlideAnimation glideAnimation) {

                Bitmap b = null;

                try {
                    mImageFull.setImageDrawable((GlideBitmapDrawable) resource);
                } catch (ClassCastException e) {
                    mImageFull.setImageDrawable((GifDrawable) resource);
                }

                mImageFull.setDrawingCacheEnabled(true);

                mImageFull.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                mImageFull.layout(0, 0, mImageFull.getMeasuredWidth(), mImageFull.getMeasuredHeight());

                mImageFull.buildDrawingCache(true);
                try{
                    b = Bitmap.createBitmap(mImageFull.getDrawingCache());
                } catch (Exception e) {
                    isCopyright = true;
                }

                mImageFull.setDrawingCacheEnabled(false);

                final Bitmap finalB = b;
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {

                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        try {
                            OutputStream os = new FileOutputStream(file);
                            finalB.compress(Bitmap.CompressFormat.JPEG, 100, os);

                        } catch (Exception e) {
                            System.out.println(e);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String string) {
                        imagePath = String.valueOf(file);
                        didFinish = true;
                        long length = file.length();
                        length = length/1024;
                        mFileSize.setText("" + length + "Kb");
                    }

                }.execute();

            }
        });
    }

    private void startCropImage() {

        Intent intent = new Intent(this, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, imagePath);
        intent.putExtra(CropImage.SCALE, true);

        intent.putExtra(CropImage.ASPECT_X, 1);
        intent.putExtra(CropImage.ASPECT_Y, 1);

        intent.putExtra(CropImage.OUTPUT_X, 400);
        intent.putExtra(CropImage.OUTPUT_Y, 400);

        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
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

                bitmap = BitmapFactory.decodeFile(imagePath);
                mImageFull.setImageBitmap(bitmap);
                break;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
