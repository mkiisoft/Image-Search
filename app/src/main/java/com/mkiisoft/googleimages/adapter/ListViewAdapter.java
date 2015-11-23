package com.mkiisoft.googleimages.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mkiisoft.googleimages.R;
import com.mkiisoft.googleimages.utils.OnSwipeTouchListener;
import com.mkiisoft.googleimages.utils.Utils;
import com.mkiisoft.googleimages.weather.CloudFogView;
import com.mkiisoft.googleimages.weather.CloudMoonView;
import com.mkiisoft.googleimages.weather.CloudRainView;
import com.mkiisoft.googleimages.weather.CloudSnowView;
import com.mkiisoft.googleimages.weather.CloudSunView;
import com.mkiisoft.googleimages.weather.CloudThunderView;
import com.mkiisoft.googleimages.weather.CloudView;
import com.mkiisoft.googleimages.weather.MoonView;
import com.mkiisoft.googleimages.weather.SunView;
import com.mkiisoft.googleimages.weather.WindView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mariano on 11/8/15.
 */
public class ListViewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, String>> data;

    HashMap<String, String> resultp = new HashMap<String, String>();

    // Views
    private LinearLayout mForecastView;
    private LinearLayout mForecastIcon;
    private TextView mTempHigh;
    private TextView mTempLow;

    // Weather Views
    private SunView sunView;
    private WindView windView;
    private MoonView moonView;
    private CloudView cloudView;
    private CloudThunderView thunderView;
    private CloudRainView rainView;
    private CloudFogView fogView;
    private CloudSunView cloudSunView;
    private CloudMoonView cloudMoonView;
    private CloudSnowView cloudSnowView;

    // Varibles
    private int celsiustempLow;
    private int celsiustempHigh;

    // Temperature Type
    String Fahrenheit = "\u00B0F";
    String Celsius    = "\u00B0C";

    public ListViewAdapter(Context context,
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

    public View getView(final int position, final View convertView, ViewGroup parent) {

        final String code = "code";
        final String date = "date";
        final String high = "high";
        final String low  = "low";

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.forecast, parent, false);

        resultp = data.get(position);

        sunView       = new SunView(context);
        windView      = new WindView(context);
        moonView      = new MoonView(context);
        cloudView     = new CloudView(context);
        thunderView   = new CloudThunderView(context);
        rainView      = new CloudRainView(context);
        fogView       = new CloudFogView(context);
        cloudSunView  = new CloudSunView(context);
        cloudMoonView = new CloudMoonView(context);
        cloudSnowView = new CloudSnowView(context);

        cloudView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        sunView.setBgColor(Color.parseColor("#00FFFFFF"));
        sunView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        windView.setBgColor(Color.parseColor("#00FFFFFF"));
        windView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        moonView.setBgColor(Color.parseColor("#00FFFFFF"));
        moonView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        fogView.setBgColor(Color.parseColor("#00FFFFFF"));
        fogView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        rainView.setBgColor(Color.parseColor("#00FFFFFF"));
        rainView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        thunderView.setBgColor(Color.parseColor("#00FFFFFF"));
        thunderView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        cloudSunView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudSunView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        cloudMoonView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudMoonView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

        cloudSnowView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudSnowView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));


        mForecastView = (LinearLayout) itemView.findViewById(R.id.forecast_view);
        mForecastIcon = (LinearLayout) itemView.findViewById(R.id.forecast_icon);
        mTempHigh     = (TextView)     itemView.findViewById(R.id.temp_low);
        mTempLow      = (TextView)     itemView.findViewById(R.id.temp_high);

        mForecastIcon.removeAllViews();

        int lows  = Integer.parseInt(resultp.get(low));
        int highs = Integer.parseInt(resultp.get(high));
        int codes = Integer.parseInt(resultp.get(code));

        float celsiusLow   = ((lows - 32) * 5 / 9);
        float celsiusHigh  = ((highs - 32) * 5 / 9);
        celsiustempLow     = Math.round(celsiusLow);
        celsiustempHigh    = Math.round(celsiusHigh);

        mTempHigh.setText(resultp.get(low) + " " + Fahrenheit);
        mTempLow.setText(resultp.get(high) + " " + Fahrenheit);

        if (codes >= 26 && codes <= 30) {
            mForecastIcon.addView(cloudView);
        } else if (codes >= 20 && codes <= 21) {
            mForecastIcon.addView(fogView);
        } else if (codes == 32){
            mForecastIcon.addView(sunView);
        } else if (codes >= 33 && codes <= 34) {
            mForecastIcon.addView(cloudSunView);
        } else if (codes >= 37 && codes <= 39) {
            mForecastIcon.addView(thunderView);
        } else if (codes == 4) {
            mForecastIcon.addView(thunderView);
        } else if (codes == 24){
            mForecastIcon.addView(windView);
        } else if (codes >= 9 && codes <= 12) {
            mForecastIcon.addView(rainView);
        } else if (codes == 31) {
            mForecastIcon.addView(moonView);
        } else if ((codes >= 5 && codes <= 8) || (codes >= 13 && codes <= 19)) {
            mForecastIcon.addView(cloudSnowView);
        } else if (codes == 3200) {
            mForecastIcon.addView(sunView);
        }

        return itemView;
    }
}