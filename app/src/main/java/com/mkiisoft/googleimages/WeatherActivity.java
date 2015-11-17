package com.mkiisoft.googleimages;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mkiisoft.googleimages.utils.ResizeAnimation;
import com.mkiisoft.googleimages.utils.SquareImageView;
import com.mkiisoft.googleimages.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by mariano-zorrilla on 17/11/15.
 */
public class WeatherActivity extends AppCompatActivity {

    private JSONObject jsonResponse;
    final ArrayList arraylist = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> result = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> data;
    private SquareImageView mWeatherImage;
    private String mApiURL;
    private String mApiWeather;
    private ActionBar mAction;
    private String city = "Paris, France";
    private TextView mTitleCity;
    private Typeface font;
    private Typeface thin;
    private View mLineColor;
    private SquareImageView mWeatherIcon;
    private TextView mTextTepm;
    private TextView mTextType;
    private RelativeLayout mMainBg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_weather);
        setStatusBarTranslucent(true);

        mAction = getSupportActionBar();
        mAction.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAction.setTitle("");
        
        mMainBg = (RelativeLayout) findViewById(R.id.main_bg);

        font = Typeface.createFromAsset(getAssets(), "thin.otf");
        thin = Typeface.createFromAsset(getAssets(), "ultra.otf");

        mApiURL = getResources().getString(R.string.apiurl);
        mApiWeather = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D\"" + city + "\")&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        mWeatherImage = (SquareImageView) findViewById(R.id.weather_main);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        mLineColor = (View) findViewById(R.id.line_color);
        mTitleCity.setTypeface(font);

        mWeatherIcon = (SquareImageView) findViewById(R.id.weather_img);

        mTextTepm = (TextView) findViewById(R.id.text_temp);
        mTextType = (TextView) findViewById(R.id.text_type);
        mTextTepm.setTypeface(thin);
        mTextType.setTypeface(font);

        AsyncConnection(mApiURL + city);
    }

    private class WeatherApi {

        private AsyncHttpClient client = new AsyncHttpClient();

        public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.get(getAbsoluteUrl(url), params, responseHandler);
        }

        public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.post(getAbsoluteUrl(url), params, responseHandler);
        }

        private String getAbsoluteUrl(String relativeUrl) {
            return relativeUrl;
        }
    }

    public void AsyncConnection(String urlConnection) {

        new WeatherApi().get(urlConnection, null, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {

                try {

                    JSONObject jsonImages = new JSONObject(Utils.decodeUTF8(response));
                    Integer status = jsonImages.getInt("responseStatus");

                    if (status == 403) {
                        String details = jsonImages.getString("responseDetails");
                        Log.e("error", details);
                    }

                    if (jsonImages.getJSONObject("responseData") != null) {
                        jsonResponse = jsonImages.getJSONObject("responseData");
                    }

                    if (status == 200) {
                        JSONArray jsonResults = jsonResponse.getJSONArray("results");

                        for (int imgs = 0; imgs < jsonResults.length(); imgs++) {

                            HashMap<String, String> images = new HashMap<String, String>();

                            JSONObject imagesResult = jsonResults.getJSONObject(imgs);

                            String url = imagesResult.getString("url");

                            images.put("images", url);

                            arraylist.add(images);

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int ran = Utils.randInt(0, 7);
                                data = arraylist;
                                result = data.get(ran);
                                String img = result.get("images");
                                Glide.with(WeatherActivity.this).load(img).asBitmap().into(new SimpleTarget() {
                                    @Override
                                    public void onResourceReady(Object resource, GlideAnimation glideAnimation) {

                                        mTitleCity.setText(city);
                                        CharSequence text = mTitleCity.getText();
                                        float width = mTitleCity.getPaint().measureText(text, 0, text.length());
                                        int textWidth = Math.round(width);
                                        float percent = (float) textWidth/120 * 100;
                                        int newWidth  = (int) percent;
                                        mWeatherImage.setImageBitmap((Bitmap) resource);
                                        final ResizeAnimation resizeAnimation = new ResizeAnimation(mLineColor, newWidth);
                                        resizeAnimation.setDuration(600);
                                        resizeAnimation.setStartOffset(400);
                                        mLineColor.startAnimation(resizeAnimation);
                                        mWeatherImage.animate().alpha(1).setDuration(800).withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTitleCity.setAlpha(0);
                                                mTitleCity.setTranslationY(-50);
                                                mTitleCity.animate().alpha(1).translationY(0).setStartDelay(200).setDuration(600).setInterpolator(new DecelerateInterpolator());
                                                AsyncWeather(mApiWeather);
                                            }
                                        });

                                    }
                                });
                            }
                        });
                    }

                } catch (Exception e) {
                    System.out.print(e);
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    public void AsyncWeather(String urlConnection) {
        new WeatherApi().get(urlConnection, null, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {
                try {
                    JSONObject jsonWeather = new JSONObject(Utils.decodeUTF8(response));

                    Log.e("Temp", "" + Utils.decodeUTF8(response));

                    JSONObject queryObject = jsonWeather.getJSONObject("query");
                    JSONObject resultObject = queryObject.getJSONObject("results");
                    JSONObject channelObject = resultObject.getJSONObject("channel");
                    JSONObject itemObject = channelObject.getJSONObject("item");
                    JSONObject condition = itemObject.getJSONObject("condition");

                    final String temp = condition.getString("temp");
                    final String text = condition.getString("text");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(R.raw.cloudy).asGif().into(mWeatherIcon);
                            mWeatherIcon.setTranslationX(-mWeatherIcon.getWidth());
                            mWeatherIcon.setAlpha(0f);
                            mWeatherIcon.animate().alpha(1f).translationX(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());
                            Utils.animateTextView(0, Integer.parseInt(temp), mTextTepm);
                            mTextTepm.setTranslationY(+mTextTepm.getHeight());
                            mTextType.setTranslationY(+mTextType.getHeight());
                            mTextTepm.setAlpha(0);
                            mTextType.setAlpha(0);
                            mTextTepm.animate().alpha(1).translationY(0).setDuration(1200).setInterpolator(new DecelerateInterpolator());
                            mTextType.animate().alpha(1).setStartDelay(200).translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());

                            Utils.animateBetweenColors(mMainBg, Color.parseColor("#23282E"), Color.parseColor("#031e2b"), 1500);
                        }
                    });

                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
