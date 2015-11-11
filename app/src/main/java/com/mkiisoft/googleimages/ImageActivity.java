package com.mkiisoft.googleimages;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mkiisoft.googleimages.utils.ScaleImageView;
import com.mkiisoft.googleimages.utils.crop.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by mariano on 11/8/15.
 */
public class ImageActivity extends AppCompatActivity {

    private ScaleImageView mImageFull;
    private TextView mButtonCancel;
    private TextView mButtonCrop;
    private String mCopyright;
    private String imagePath;
    private String path;
    private File dir;
    private File file;
    private boolean didFinish = false;
    private boolean isCopyright = false;
    private boolean mNotInternet;

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    public static final int REQUEST_CODE_CROP_IMAGE = 0x3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        final String url = extras.getString("url");
        final String id = extras.getString("id");

        if (intent != null && intent.getStringExtra("not_internet") != null) {
            mNotInternet = true;
        } else {
            mNotInternet = false;
        }

        dir = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang");
        file = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang", id + ".jpg");

        mCopyright = getResources().getString(R.string.copyright);

        mImageFull = (ScaleImageView) findViewById(R.id.image_full);
        mButtonCancel = (TextView) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFolder();
            }
        });

        mButtonCrop = (TextView) findViewById(R.id.button_crop);
        mButtonCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (didFinish && !isCopyright && !mNotInternet) {
                    startCropImage();
                } else if (isCopyright) {
                    Toast.makeText(ImageActivity.this, mCopyright, Toast.LENGTH_SHORT).show();
                } else if (mNotInternet){
                    mButtonCrop.setEnabled(false);
                }
            }
        });

        Glide.with(this)
                .load(url)
                .asBitmap()
                .into(new SimpleTarget() {
                    @Override
                    public void onResourceReady(Object resource, GlideAnimation glideAnimation) {

                        mImageFull.setImageBitmap((Bitmap) resource);

                        if(!mNotInternet){
                            mImageFull.setDrawingCacheEnabled(true);

                            mImageFull.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                            mImageFull.layout(0, 0, mImageFull.getMeasuredWidth(), mImageFull.getMeasuredHeight());

                            mImageFull.buildDrawingCache(true);
                            try {
                                resource = Bitmap.createBitmap(mImageFull.getDrawingCache());
                            } catch (Exception e) {
                                isCopyright = true;
                            }
                            mImageFull.setDrawingCacheEnabled(false);

                            final Bitmap b = (Bitmap) resource;
                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected String doInBackground(Void... params) {

                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }

                                    try {
                                        OutputStream os = new FileOutputStream(file);
                                        b.compress(Bitmap.CompressFormat.JPEG, 100, os);

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
                                    length = length / 1024;
                                }

                            }.execute();
                        }

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_img_broken).skipMemoryCache(false).into(mImageFull);
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

    public void deleteFolder(){
        File dir = new File(android.os.Environment.getExternalStorageDirectory() + "/QMerang");

        if(dir.exists()){
            if (dir.isDirectory())
            {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++)
                {
                    new File(dir, children[i]).delete();
                    finish();
                }
            }
        }
    }
}
