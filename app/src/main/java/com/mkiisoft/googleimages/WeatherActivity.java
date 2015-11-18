package com.mkiisoft.googleimages;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mkiisoft.googleimages.utils.CubicBezierInterpolator;
import com.mkiisoft.googleimages.utils.ResizeAnimation;
import com.mkiisoft.googleimages.utils.SquareImageView;
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
    private String mApiWeatherFull;
    private ActionBar mAction;
    private String city = "New York City";
    private TextView mTitleCity;
    private Typeface font;
    private Typeface thin;
    private View mLineColor;
    private LinearLayout mWeatherIcon;
    private TextView mTextTemp;
    private TextView mTextType;
    private RelativeLayout mMainBg;

//    public enum Conditions {
//
//        cloudy(26),
//        mostlycloudynight(27),
//        mostlycloudyday(28),
//        partlycloudynight(29),
//        partlycloudyday(30),
//        sun(32),
//        thunder(4),
//        rain(10);
//
//        private int _value;
//
//        Conditions(int Value) {
//            this._value = Value;
//        }
//
//        public int getValue() {
//            return _value;
//        }
//
//        public static Conditions fromInt(int i) {
//            for (Conditions b : Conditions.values()) {
//                if (b.getValue() == i) {
//                    return b;
//                }
//            }
//            return null;
//        }
//    }
//
//    int value;
//    Conditions conditions = Conditions.fromInt(value);

    public static final int FAB_STATE_COLLAPSED = 0;
    public static final int FAB_STATE_EXPANDED = 1;

    public static int FAB_CURRENT_STATE = FAB_STATE_COLLAPSED;

    View mFab, mExpandedView, mCollapseFabButton;
    EditText edit;
    private CloudView cloudView;
    private SunView sunView;
    private MoonView moonView;
    private CloudSunView cloudSunView;
    private CloudThunderView thunderView;
    private CloudRainView rainView;
    private CloudFogView fogView;
    private CloudMoonView cloudMoonView;
    private CloudSnowView cloudSnowView;

    String Fahrenheit = "\u00B0F";
    String Celsius    = "\u00B0C";
    int celsiustemp;
    private String code, temp;
    private String mSearchBox;
    private String mNextColor = "#23282E";

    private boolean isFistTime = true;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_weather);
        setStatusBarTranslucent(true);

        mAction = getSupportActionBar();
        mAction.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAction.setTitle("");

        sunView       = new SunView(WeatherActivity.this);
        moonView      = new MoonView(WeatherActivity.this);
        cloudView     = new CloudView(WeatherActivity.this);
        thunderView   = new CloudThunderView(WeatherActivity.this);
        rainView      = new CloudRainView(WeatherActivity.this);
        fogView       = new CloudFogView(WeatherActivity.this);
        cloudSunView  = new CloudSunView(WeatherActivity.this);
        cloudMoonView = new CloudMoonView(WeatherActivity.this);
        cloudSnowView = new CloudSnowView(WeatherActivity.this);

        cloudView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        sunView.setBgColor(Color.parseColor("#00FFFFFF"));
        sunView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        fogView.setBgColor(Color.parseColor("#00FFFFFF"));
        fogView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        rainView.setBgColor(Color.parseColor("#00FFFFFF"));
        rainView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        thunderView.setBgColor(Color.parseColor("#00FFFFFF"));
        thunderView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        cloudSunView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudSunView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        cloudMoonView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudMoonView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        cloudSnowView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudSnowView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        mMainBg = (RelativeLayout) findViewById(R.id.main_bg);

        font = Typeface.createFromAsset(getAssets(), "thin.otf");
        thin = Typeface.createFromAsset(getAssets(), "ultra.otf");

        mApiURL = getResources().getString(R.string.apiurl);
        mApiWeather     = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D\"";
        mApiWeatherFull = "\")&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
        mSearchBox  = getResources().getString(R.string.searchbox);

        mWeatherImage = (SquareImageView) findViewById(R.id.weather_main);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        mLineColor = findViewById(R.id.line_color);
        mTitleCity.setTypeface(font);

        mFab = findViewById(R.id.fab);
        mExpandedView = findViewById(R.id.expanded_view);
        mCollapseFabButton = findViewById(R.id.act_collapse);

        edit = (EditText) findViewById(R.id.search_box);
        edit.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && edit.getText().toString().trim().length() > 0) {
                    if (v != null) {
                        mCollapseFabButton.performClick();
                    }
                    return true;
                } else if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && edit.getText().toString().trim().length() == 0) {
                    Toast.makeText(WeatherActivity.this, mSearchBox, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        mWeatherIcon = (LinearLayout) findViewById(R.id.weather_img);

        mTextTemp = (TextView) findViewById(R.id.text_temp);
        mTextType = (TextView) findViewById(R.id.text_type);
        mTextTemp.setTypeface(thin);
        mTextType.setTypeface(font);

        AsyncConnection(mApiURL + city);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                revealView(mExpandedView);
                FAB_CURRENT_STATE = FAB_STATE_EXPANDED;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFab.setVisibility(View.GONE);
                    }
                }, 50);

                mCollapseFabButton.animate().rotationBy(220).setDuration(250).start();


            }
        });

        mCollapseFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edit.getText().toString().trim().length() == 0){

                }else{
                    arraylist.clear();
                    result.clear();
                    data.clear();
                    initViews();
                    searchReset();
                    city = edit.getText().toString();
                    AsyncConnection(mApiURL + city);

                    if(view != null){
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

                hideView(mExpandedView);

                FAB_CURRENT_STATE = FAB_STATE_COLLAPSED;

                mCollapseFabButton.animate().rotationBy(-220).setDuration(200).start();
            }
        });

        mTextType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoopAnimBottom(mTextType);
                LoopAnimTop(mTextTemp);
            }
        });

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

                                        mTitleCity.setTranslationY(-50);
                                        mTitleCity.setText(city);
                                        CharSequence text = mTitleCity.getText();
                                        float width = mTitleCity.getPaint().measureText(text, 0, text.length());
                                        int textWidth = Math.round(width);
                                        float percent = (float) textWidth / 120 * 100;
                                        int newWidth = (int) percent;
                                        final Bitmap finalBitmap = (Bitmap) resource;
                                        mWeatherImage.animate().alpha(0.2f).setDuration(400).withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                mWeatherImage.setAlpha(0.2f);
                                                mWeatherImage.setImageBitmap(finalBitmap);
                                                mWeatherImage.animate().alpha(1).setDuration(800).withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTitleCity.animate().alpha(1).translationY(0).setStartDelay(200).setDuration(600).setInterpolator(new DecelerateInterpolator());
                                                        if (!isFistTime) {
                                                            city = edit.getText().toString();
                                                            isFistTime = false;
                                                        }
                                                        AsyncWeather(mApiWeather + city + mApiWeatherFull);
                                                    }
                                                });
                                            }
                                        });
                                        final ResizeAnimation resizeAnimation = new ResizeAnimation(mLineColor, newWidth);
                                        resizeAnimation.setDuration(600);
                                        resizeAnimation.setStartOffset(400);
                                        mLineColor.startAnimation(resizeAnimation);

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
                    final JSONObject condition = itemObject.getJSONObject("condition");

                    temp = condition.getString("temp");
                    code = condition.getString("code");

                    Log.e("temp", temp);
                    Log.e("code", code);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            int temps = Integer.parseInt(temp);
                            int codes = Integer.parseInt(code);

                            float celsiusf = ((temps - 32) * 5 / 9);
                            celsiustemp    = Math.round(celsiusf);

                            Log.e("que codigo", "" + codes);
                            Log.e("celsius", "" + celsiustemp);

                            if (codes >= 26 && codes <= 30) {
                                mWeatherIcon.addView(cloudView);
                            } else if (codes >= 20 && codes <= 21) {
                                mWeatherIcon.addView(fogView);
                            } else if (codes == 32){
                                mWeatherIcon.addView(sunView);
                            } else if (codes >= 33 && codes <= 34) {
                                mWeatherIcon.addView(cloudSunView);
                            } else if (codes == 4) {
                                mWeatherIcon.addView(thunderView);
                            } else if (codes >= 11 && codes <= 12) {
                                mWeatherIcon.addView(rainView);
                            } else if (codes == 31) {
                                mWeatherIcon.addView(moonView);
                            } else if ((codes >= 5 && codes <= 10) || (codes >= 13 && codes <= 19)) {
                                mWeatherIcon.addView(cloudSnowView);
                            } else if (codes == 3200) {
                                mWeatherIcon.addView(sunView);
                            }

                            mWeatherIcon.setTranslationX(-mWeatherIcon.getWidth());
                            mWeatherIcon.animate().alpha(1f).translationX(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());
                            if(!isFistTime){
                                Utils.animateTextView(Integer.parseInt(mTextTemp.getText().toString()), Integer.parseInt(temp), mTextTemp);
                            }else{
                                Utils.animateTextView(0, Integer.parseInt(temp), mTextTemp);
                            }

                            mTextTemp.setTranslationY(+mTextTemp.getHeight());
                            mTextType.setTranslationY(-mTextType.getHeight());

                            mTextTemp.animate().alpha(1).translationY(0).setDuration(1200).setInterpolator(new DecelerateInterpolator());
                            mTextType.animate().alpha(1).translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());

                            if(temps >= 50 && temps <= 65){
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#031e2b"), 1500);
                                mNextColor = "#031e2b";
                            } else if (temps >= 32 && temps <= 49){
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#062f42"), 1500);
                                mNextColor = "#062f42";
                            } else if (temps <= 31){
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#063d56"), 1500);
                                mNextColor = "#063d56";
                            } else if (temps >= 66 && temps <= 85){
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#211501"), 1500);
                                mNextColor = "#211501";
                            } else if (temps >= 86 && temps <= 95){
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#301f02"), 1500);
                                mNextColor = "#301f02";
                            } else if (temps >= 96){
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#422a04"), 1500);
                                mNextColor = "#422a04";
                            }
                        }
                    });

                } catch (Exception e) {
                    System.out.print(e);
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


    public void revealView(View myView) {


        int cx = (mFab.getLeft() + mFab.getRight()) / 2;
        int cy = (mFab.getTop() + mFab.getBottom()) / 2;

        int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        anim.setDuration(300);

        myView.setVisibility(View.VISIBLE);

        slideView(edit);

        anim.start();


    }

    public void hideView(final View myView) {


        int cx = (mFab.getLeft() + mFab.getRight()) / 2;
        int cy = (mFab.getTop() + mFab.getBottom()) / 2;

        int initialRadius = myView.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
        anim.setDuration(300);
        anim.setInterpolator(getLinearOutSlowInInterpolator());

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                myView.setVisibility(View.INVISIBLE);

            }
        });


        //Normally I would restore visibility when the hide animation has ended, but it doesn't look as good, so I'm doing it earlier.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFab.setVisibility(View.VISIBLE);
            }
        }, 200);

        anim.start();

    }

    //Animation to slide the action buttons
    public void slideView(View view) {
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 112, 0);
        slide.setDuration(500);
        slide.setInterpolator(getLinearOutSlowInInterpolator());
        slide.start();
    }


    //Code for these: https://gist.github.com/chris95x8/4d74591bed75fd151799
    public static Interpolator getLinearOutSlowInInterpolator() {
        //Decelerate Interpolator - For elements that enter the screen
        return new CubicBezierInterpolator(0, 0, 0.2, 1);
    }

    public static Interpolator getFastInSlowOutInterpolator() {
        //Ease In Out Interpolator - For elements that change position while staying in the screen
        return new CubicBezierInterpolator(0.4, 0, 0.2, 1);
    }

    public void LoopAnimBottom(final TextView view){
        view.animate().alpha(0).translationY(+view.getHeight()).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(0);
                if(mTextType.getText().toString().contentEquals(Fahrenheit)){
                    view.setText(Celsius);
                    mTextTemp.setText("" + celsiustemp);
                }else{
                    view.setText(Fahrenheit);
                    mTextTemp.setText("" + temp);
                }
                view.setTranslationY(-view.getHeight());
                view.animate().alpha(1).translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator());
            }
        });
    }

    public void LoopAnimTop(final TextView view){
        view.animate().alpha(0).translationY(-view.getHeight()).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(0);
                view.setTranslationY(+view.getHeight());
                view.animate().alpha(1).translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator());
            }
        });
    }

    public void initViews(){
        final ResizeAnimation resizeAnimation = new ResizeAnimation(mLineColor, 0);
        resizeAnimation.setDuration(400);
        mLineColor.startAnimation(resizeAnimation);
        mTitleCity.animate()
                .alpha(0)
                .translationY(-50)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTitleCity.setAlpha(0);
                        mTitleCity.setTranslationY(-50);
                    }
                });

    }

    public void searchReset(){
        mWeatherIcon.animate()
                .alpha(0)
                .translationX(-mWeatherIcon.getWidth())
                .setDuration(400)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mWeatherIcon.setTranslationX(-mWeatherIcon.getWidth());
                        mWeatherIcon.setAlpha(0f);
                        mWeatherIcon.removeAllViews();
                        mWeatherIcon.invalidate();
                    }
                });

        mTextTemp.animate()
                .alpha(0)
                .translationY(-mTextTemp.getHeight())
                .setDuration(400)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTextTemp.setTranslationY(+mTextTemp.getHeight());
                        mTextTemp.setAlpha(0);
                    }
                });

        mTextType.animate()
                .alpha(0)
                .translationY(+mTextType.getHeight())
                .setDuration(400)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTextType.setTranslationY(-mTextType.getHeight());
                        mTextType.setAlpha(0);
                    }
                });
        mTextType.setText(Fahrenheit);
    }

}
