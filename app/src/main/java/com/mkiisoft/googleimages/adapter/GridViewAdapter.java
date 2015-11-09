package com.mkiisoft.googleimages.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.mkiisoft.googleimages.ImageActivity;
import com.mkiisoft.googleimages.R;
import com.mkiisoft.googleimages.utils.SquareImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.squareup.picasso.Picasso;

/**
 * Created by mariano on 11/8/15.
 */
public class GridViewAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, String>> data;
    //ImageLoader imageLoader;
    HashMap<String, String> resultp = new HashMap<String, String>();

    public GridViewAdapter(Context context,
                           ArrayList<HashMap<String, String>> arraylist) {
        this.context = context;
        data = arraylist;

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final String img    = "images";
        final String imgId  = "imageId";

        final SquareImageView appicon;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.grid_item, parent, false);

        resultp = data.get(position);

        itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                resultp = data.get(position);
                Intent intent = new Intent(context, ImageActivity.class);

                intent.putExtra("url", resultp.get(img));
                intent.putExtra("id" , resultp.get(imgId));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);
            }
        });

        appicon = (SquareImageView) itemView.findViewById(R.id.grid_thumb_img);

        Glide.with(context)
                .load(resultp.get(img))
                .placeholder(R.drawable.ic_loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(appicon);

        return itemView;
    }
}