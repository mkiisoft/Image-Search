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
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mkiisoft.googleimages.adapter.ListViewAdapter;
import com.mkiisoft.googleimages.utils.CubicBezierInterpolator;
import com.mkiisoft.googleimages.utils.OnSwipeTouchListener;
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
import com.mkiisoft.googleimages.weather.WindView;
import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.CertSelector;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by mariano-zorrilla on 17/11/15.
 */

public class WeatherActivity extends AppCompatActivity {

    // API Response
    private JSONObject jsonResponse;

    // Array list from APIs
    final ArrayList arraylist = new ArrayList<>();
    final ArrayList arrayhistory = new ArrayList<>(11);
    final ArrayList arraylistForecast = new ArrayList<>();

    // HashMap to get Key/Value
    HashMap<String, String> result = new HashMap<>();
    HashMap<String, String> resultHistory = new HashMap<>();

    // ArrayList to get position
    ArrayList<HashMap<String, String>> data;
    ArrayList<HashMap<String, String>> dataHistory;

    // API Strings URIs
    private String mApiURL, mApiCatchQuery, mApiCatchWoeid;

    private ActionBar mAction;
    private String city = "New York City";
    private Typeface font, thin;
    private View mLineColor;
    private RelativeLayout mMainBg;
    private LinearLayout mWeatherIcon;
    private SquareImageView mWeatherImage;
    private TextView mTitleCity, mTextTemp, mTextType;

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

    ImageButton mFab, mCollapseFabButton;
    RelativeLayout mExpandedView;
    EditText edit;
    ListView mHistoryList;

    // Weather Views
    private CloudView cloudView;
    private SunView sunView;
    private WindView windView;
    private MoonView moonView;
    private CloudSunView cloudSunView;
    private CloudThunderView thunderView;
    private CloudRainView rainView;
    private CloudFogView fogView;
    private CloudMoonView cloudMoonView;
    private CloudSnowView cloudSnowView;

    String Fahrenheit = "\u00B0F";
    String Celsius = "\u00B0C";
    int celsiustemp;
    private String code, temp;
    private String mSearchBox;
    private String mNextColor = "#23282E";

    private GestureDetector gesture;
    private AVLoadingIndicatorView mProgress;

    // swipe views
    private RelativeLayout mMainCity, mMainTemp, mChildTemp;
    private FrameLayout mMainBtn;
    private ListView mListForecast;

    // Adapter
    private ListViewAdapter mAdapter;
    private SimpleAdapter mAdapterHistory;
    private SwingLeftInAnimationAdapter swingLeftInAnimationAdapter;

    // Access to Utils
    Utils.ApiCall mApiCall;
    Utils.ApiCallSync mApiCatch;

    private RequestParams params;

    // Variables
    private boolean isFistTime  = true;
    private boolean fromHistory = false;
    private String queryURL = "New York City";

    // Catch Weather Cities
    private String mName, mCode;
    private int mWoeid;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_weather);
        setStatusBarTranslucent(true);

        mApiCall  = new Utils.ApiCall();
        mApiCatch = new Utils.ApiCallSync();
        params    = new RequestParams();

        mAction = getSupportActionBar();
        mAction.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAction.setTitle("");

        initCustomWeatherViews();
        decorateCustomWeatherViews();

        mProgress = (AVLoadingIndicatorView) findViewById(R.id.progress_balls);

        mMainBg = (RelativeLayout) findViewById(R.id.main_bg);

        // animated views swipe up and down
        mMainCity = (RelativeLayout) findViewById(R.id.city_main);
        mMainTemp = (RelativeLayout) findViewById(R.id.temp_main);
        mMainBtn = (FrameLayout) findViewById(R.id.btn_main);
        mChildTemp = (RelativeLayout) findViewById(R.id.temp_child);

        mChildTemp.setAlpha(0);
        mChildTemp.setEnabled(false);
        mChildTemp.setTranslationY(+mChildTemp.getHeight());

        mListForecast = (ListView) findViewById(R.id.list_forecast);

        mAdapter = new ListViewAdapter(WeatherActivity.this, arraylistForecast);
        swingLeftInAnimationAdapter = new SwingLeftInAnimationAdapter(mAdapter);
        swingLeftInAnimationAdapter.setAbsListView(mListForecast);
        mListForecast.setAdapter(swingLeftInAnimationAdapter);

        mListForecast.setOnTouchListener(new OnSwipeTouchListener(this) {

            public void onSwipeTop() {
                if (!mChildTemp.isEnabled()) {
                    swipeUp(true);
                }
            }

            public void onSwipeRight() {
            }

            public void onSwipeLeft() {
            }

            public void onSwipeBottom() {
                if (mChildTemp.isEnabled()) {
                    swipeDown(true);
                }
            }
        });

        mMainBg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mMainBg.getRootView().getHeight() - mMainBg.getHeight();
                if (heightDiff > 50) {
                    WeatherActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
            }
        });
        mMainBg.setOnTouchListener(new OnSwipeTouchListener(this) {

            public void onSwipeTop() {
                if (!mChildTemp.isEnabled()) {
                    swipeUp(true);
                }
            }

            public void onSwipeRight() {
                int randoms = Utils.randInt(0, 7);
                result = data.get(randoms);
                String images = result.get("images");
                Glide.with(WeatherActivity.this).load(images).override(800, 800).into(mWeatherImage);
            }

            public void onSwipeLeft() {
                int randoms = Utils.randInt(0, 7);
                result = data.get(randoms);
                String images = result.get("images");
                Glide.with(WeatherActivity.this).load(images).override(800, 800).into(mWeatherImage);
            }

            public void onSwipeBottom() {
                if (mChildTemp.isEnabled()) {
                    swipeDown(true);
                }
            }
        });

        font = Typeface.createFromAsset(getAssets(), "thin.otf");
        thin = Typeface.createFromAsset(getAssets(), "ultra.otf");

        mApiURL = getResources().getString(R.string.apiurl);
        mApiCatchQuery = "http://catchweather.com/api/query.php?key=ABARAJAMEENLABANIERANENA&format=json&q=";
        mApiCatchWoeid = "http://catchweather.com/api/weather.php?key=ABARAJAMEENLABANIERANENA&format=json&woeid=";

        mSearchBox = getResources().getString(R.string.searchbox);

        mWeatherImage = (SquareImageView) findViewById(R.id.weather_main);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        mLineColor = findViewById(R.id.line_color);
        mTitleCity.setTypeface(font);

        mFab = (ImageButton) findViewById(R.id.fab);
        mExpandedView = (RelativeLayout) findViewById(R.id.expanded_view);
        mCollapseFabButton = (ImageButton) findViewById(R.id.act_collapse);

        mHistoryList = (ListView) findViewById(R.id.list_history);
        mHistoryList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
        mAdapterHistory = new SimpleAdapter(this, arrayhistory, R.layout.history_item, new String[]{"history"},
                new int[]{R.id.item_text_history});
        mHistoryList.setAdapter(mAdapterHistory);

        mHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromHistory = true;
                dataHistory = arrayhistory;
                resultHistory = dataHistory.get(position);
                queryURL = resultHistory.get("history");
                edit.setText(queryURL);
                mCollapseFabButton.performClick();
            }
        });

        edit = (EditText) findViewById(R.id.search_box);

//        edit.addTextChangedListener(new TextWatcher() {
//
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                didChange = false;
//            }
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(count > 1){
//                    didChange = true;
//                } else {
//                    didChange = false;
//                }
//            }
//
//            private Timer timer = new Timer();
//            private final long DELAY = 800;
//
//            @Override
//            public void afterTextChanged(final Editable s) {
//
//                final String queryURL = edit.getText().toString();
//
//                if (didChange){
//                    mApiCatch.cancelRequest(true);
//                    timer.cancel();
//                    timer = new Timer();
//                    timer.schedule(
//                            new TimerTask() {
//                                @Override
//                                public void run() {
//                                    mApiCatch.get(mApiCatchWeather + queryURL, null, new JsonHttpResponseHandler() {
//
//                                        @Override
//                                        public void onStart() {
//
//                                        }
//
//                                        @Override
//                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                                            try {
//
//                                                JSONObject getResponse = new JSONObject("" + response);
//                                                JSONObject getPlaces = getResponse.getJSONObject("places");
//                                                int getTotal = getPlaces.getInt("total");
//                                                Log.e("total", "" + getTotal);
//
//                                                JSONArray getPlace = getPlaces.getJSONArray("place");
//                                                for (int i = 0; i < getPlace.length(); i++) {
//
//                                                    JSONObject placeObj = getPlace.getJSONObject(i);
//                                                    JSONObject attrsObj = placeObj.getJSONObject("country attrs");
//                                                    String name  = placeObj.getString("name");
//                                                    int    woeid = attrsObj.getInt("woeid");
//
//                                                    Log.e("name", name + " with ID: " + ""+woeid);
//                                                }
//
//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//
//
//                                        @Override
//                                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
//
//                                        }
//
//                                    });
//                                }
//                            },
//                            DELAY
//                    );
//                }
//            }
//        });

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

        SyncAsyncConenection(mApiCatchQuery + queryURL);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (arrayhistory.size() >= 1) {
                    mHistoryList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }

                edit.setText("");
                revealView(mExpandedView);
                FAB_CURRENT_STATE = FAB_STATE_EXPANDED;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
                WeatherActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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

                if (edit.getText().toString().trim().length() == 0) {
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                } else {
                    queryURL = edit.getText().toString();
                    HashMap<String, String> history = new HashMap<>();
                    history.put("history", edit.getText().toString());
                    if(!fromHistory){
                        arrayhistory.add(0, history);
                    }
                    fromHistory = false;
                    if (arrayhistory.size() > 10) {
                        arrayhistory.remove(10);
                    }
                    mAdapterHistory.notifyDataSetChanged();

                    arraylistForecast.clear();
                    arraylist.clear();
                    result.clear();
                    data.clear();
                    initViews();
                    searchReset();
                    city = edit.getText().toString();

                    if (view != null) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                SyncAsyncConenection(mApiCatchQuery + queryURL);
                            }
                        });
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

    public void SyncAsyncConenection(String urlConnection) {

        mApiCall.get(urlConnection, null, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    JSONObject getResponse = new JSONObject("" + response);
                    JSONObject getPlaces = getResponse.getJSONObject("places");
                    final int getTotal = getPlaces.getInt("total");
                    Log.e("total", "" + getTotal);

                    JSONArray getPlace = getPlaces.getJSONArray("place");
                    for (int i = 0; i < getPlace.length(); i++) {

                        JSONObject placeObj = getPlace.getJSONObject(i);
                        JSONObject attrsObj = placeObj.getJSONObject("country attrs");
                        mName  = placeObj.getString("name");
                        mCode  = attrsObj.getString("code");
                        mWoeid = attrsObj.getInt("woeid");
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (getTotal == 1) {
                                AsyncConnection(mApiURL + queryURL);
                            }

                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {

            }

        });

    }

    public void AsyncConnection(String urlConnection) {

        mApiCall.get(urlConnection, null, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                mChildTemp.setEnabled(true);
                mProgress.setVisibility(View.VISIBLE);
                mProgress.animate().alpha(1).setStartDelay(500).setDuration(400).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setAlpha(1);
                    }
                });

            }

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {

                try {

                    JSONObject jsonImages = new JSONObject(Utils.decodeUTF8(response));
                    final Integer status = jsonImages.getInt("responseStatus");
                    Log.e("status", ""+status);
                    Log.e("response", ""+Utils.decodeUTF8(response));

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

                            HashMap<String, String> images = new HashMap<>();

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
                                Glide.with(WeatherActivity.this).load(img).asBitmap().override(800, 800).
                                        into(new SimpleTarget() {
                                            @Override
                                            public void onResourceReady(Object resource, GlideAnimation glideAnimation) {

                                                mTitleCity.setTranslationY(-50);
                                                mTitleCity.setText(mName + " " + mCode);
                                                CharSequence text = mTitleCity.getText();
                                                float width = mTitleCity.getPaint().measureText(text, 0, text.length());
                                                int textWidth = Math.round(width);
                                                float percent = (float) textWidth / 130 * 100;
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
                                                                AsyncWeather(mApiCatchWoeid + mWoeid);
                                                            }
                                                        });
                                                    }
                                                });
                                                final ResizeAnimation resizeAnimation = new ResizeAnimation(mLineColor, newWidth);
                                                resizeAnimation.setDuration(600);
                                                resizeAnimation.setStartOffset(400);
                                                mLineColor.startAnimation(resizeAnimation);

                                            }

                                            @Override
                                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                                if (status == 200) {
                                                    int randoms = Utils.randInt(0, 7);
                                                    result = data.get(randoms);
                                                    String images = result.get("images");
                                                    Glide.with(WeatherActivity.this).load(images).override(800, 800).into(mWeatherImage);
                                                }
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
        mApiCall.get(urlConnection, null, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                mProgress.animate().alpha(0).setDuration(400).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setAlpha(0);
                        mProgress.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {
                try {
                    JSONObject jsonWeather = new JSONObject(Utils.decodeUTF8(response));

                    Log.e("Response", "" + Utils.decodeUTF8(response));

                    JSONObject resultObject = jsonWeather.getJSONObject("yw_forecast");
                    final JSONObject condition = resultObject.getJSONObject("condition");
                    JSONObject tempObject = condition.getJSONObject("temp");
                    JSONObject codeObject = condition.getJSONObject("code");

                    temp = tempObject.getString("0");
                    code = codeObject.getString("0");

                    JSONObject forecast = resultObject.getJSONObject("forecast");

//                    for(int f = 0; f < forecast.length(); f++){
                    HashMap<String, String> forecastHash = new HashMap<>();
//                        JSONObject forecastObj = forecast.getJSONObject(f);
//
//                        forecastHash.put("code", forecastObj.getString("code"));
//                        forecastHash.put("date", forecastObj.getString("date"));
//                        forecastHash.put("high", forecastObj.getString("high"));
//                        forecastHash.put("low", forecastObj.getString("low"));

                    forecastHash.put("code", "30");
                    forecastHash.put("date", "24 Nov 2015");
                    forecastHash.put("high", "20");
                    forecastHash.put("low",  "10");
//
//                        Log.e("code child", "" + forecastObj.getString("code"));
//
                    arraylistForecast.add(forecastHash);
//
//                    }

                    Log.e("fahrenheit", temp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            int temps = Integer.parseInt(temp);
                            int codes = Integer.parseInt(code);

                            float celsiusf    = ((temps - 32) * 5 / 9);
                            float fahrenheitf =((temps * 9) / 5) + 32;
                            celsiustemp = Math.round(fahrenheitf);

                            Log.e("code", "" + codes);
                            Log.e("fahrenheit", "" + celsiustemp);

                            if (codes >= 26 && codes <= 30) {
                                mWeatherIcon.addView(cloudView);
                            } else if (codes >= 20 && codes <= 21) {
                                mWeatherIcon.addView(fogView);
                            } else if (codes == 32) {
                                mWeatherIcon.addView(sunView);
                            } else if (codes >= 33 && codes <= 34) {
                                mWeatherIcon.addView(cloudSunView);
                            } else if (codes >= 37 && codes <= 39) {
                                mWeatherIcon.addView(thunderView);
                            } else if (codes == 4) {
                                mWeatherIcon.addView(thunderView);
                            } else if (codes == 24) {
                                mWeatherIcon.addView(windView);
                            } else if (codes >= 9 && codes <= 12) {
                                mWeatherIcon.addView(rainView);
                            } else if (codes == 31) {
                                mWeatherIcon.addView(moonView);
                            } else if ((codes >= 5 && codes <= 8) || (codes >= 13 && codes <= 19)) {
                                mWeatherIcon.addView(cloudSnowView);
                            } else if (codes == 3200) {
                                mWeatherIcon.addView(sunView);
                            }

                            mWeatherIcon.setTranslationX(-mWeatherIcon.getWidth());
                            mWeatherIcon.animate().alpha(1f).translationX(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());
                            if (!isFistTime) {
                                Utils.animateTextView(Integer.parseInt(mTextTemp.getText().toString()), Integer.parseInt(temp), mTextTemp);
                            } else {
                                Utils.animateTextView(0, Integer.parseInt(temp), mTextTemp);
                            }

                            mTextTemp.setTranslationY(+mTextTemp.getHeight());
                            mTextType.setTranslationY(-mTextType.getHeight());

                            mTextTemp.animate().alpha(1).translationY(0).setDuration(1200).setInterpolator(new DecelerateInterpolator());
                            mTextType.animate().alpha(1).translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());

                            if (temps >= 10 && temps <= 18) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#031e2b"), 1500);
                                mNextColor = "#031e2b";
                            } else if (temps >= 0 && temps <= 9) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#062f42"), 1500);
                                mNextColor = "#062f42";
                            } else if (temps < 0) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#063d56"), 1500);
                                mNextColor = "#063d56";
                            } else if (temps >= 19 && temps <= 29) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#211501"), 1500);
                                mNextColor = "#211501";
                            } else if (temps >= 30 && temps <= 35) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#301f02"), 1500);
                                mNextColor = "#301f02";
                            } else if (temps >= 36) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#422a04"), 1500);
                                mNextColor = "#422a04";
                            }

                            mChildTemp.setEnabled(false);
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

    public void initCustomWeatherViews() {
        sunView = new SunView(WeatherActivity.this);
        windView = new WindView(WeatherActivity.this);
        moonView = new MoonView(WeatherActivity.this);
        cloudView = new CloudView(WeatherActivity.this);
        thunderView = new CloudThunderView(WeatherActivity.this);
        rainView = new CloudRainView(WeatherActivity.this);
        fogView = new CloudFogView(WeatherActivity.this);
        cloudSunView = new CloudSunView(WeatherActivity.this);
        cloudMoonView = new CloudMoonView(WeatherActivity.this);
        cloudSnowView = new CloudSnowView(WeatherActivity.this);
    }

    public void decorateCustomWeatherViews() {

        cloudView.setBgColor(Color.parseColor("#00FFFFFF"));
        cloudView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        sunView.setBgColor(Color.parseColor("#00FFFFFF"));
        sunView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        windView.setBgColor(Color.parseColor("#00FFFFFF"));
        windView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

        moonView.setBgColor(Color.parseColor("#00FFFFFF"));
        moonView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFab.setVisibility(View.VISIBLE);
            }
        }, 200);

        anim.start();

    }

    public void slideView(View view) {
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 112, 0);
        slide.setDuration(500);
        slide.setInterpolator(getLinearOutSlowInInterpolator());
        slide.start();
    }

    public static Interpolator getLinearOutSlowInInterpolator() {
        return new CubicBezierInterpolator(0, 0, 0.2, 1);
    }

    public static Interpolator getFastInSlowOutInterpolator() {
        return new CubicBezierInterpolator(0.4, 0, 0.2, 1);
    }

    public void LoopAnimBottom(final TextView view) {
        view.animate().alpha(0).translationY(+view.getHeight()).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(0);
                if (mTextType.getText().toString().contentEquals(Celsius)) {
                    view.setText(Fahrenheit);
                    mTextTemp.setText("" + celsiustemp);
                } else {
                    view.setText(Celsius);
                    mTextTemp.setText("" + temp);
                }
                view.setTranslationY(-view.getHeight());
                view.animate().alpha(1).translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator());
            }
        });
    }

    public void LoopAnimTop(final TextView view) {
        view.animate().alpha(0).translationY(-view.getHeight()).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(0);
                view.setTranslationY(+view.getHeight());
                view.animate().alpha(1).translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator());
            }
        });
    }

    public void initViews() {
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

    public void searchReset() {
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
                        mTextType.setText(Celsius);

                    }
                });
    }


    @Override
    public void onBackPressed() {
        if (mExpandedView.getVisibility() == View.VISIBLE) {
            hideView(mExpandedView);
            FAB_CURRENT_STATE = FAB_STATE_COLLAPSED;
            mCollapseFabButton.animate().rotationBy(-220).setDuration(200).start();
        } else {
            finish();
        }
    }

    public void swipeUp(boolean enabled) {
        if (enabled) {
            mMainCity.animate().translationY(-mMainBg.getHeight()).setDuration(600).setStartDelay(200).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainTemp.animate().translationY(-mMainBg.getHeight()).setDuration(600).setStartDelay(400).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainBtn.animate().translationY(-mMainBg.getHeight()).setDuration(600).setStartDelay(300).setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mChildTemp.setEnabled(true);
                            mChildTemp.setTranslationY(0);
                            mChildTemp.animate().alpha(1).setDuration(500).setStartDelay(200).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mChildTemp.setAlpha(1);
                                    mListForecast.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swingLeftInAnimationAdapter.notifyDataSetChanged();
                                        }
                                    });

                                }
                            });
                        }
                    });
        }

    }

    public void swipeDown(boolean enabled) {
        if (enabled) {
            mChildTemp.setEnabled(false);
            mChildTemp.animate().alpha(0).setDuration(600).setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mChildTemp.setAlpha(0);
                            mChildTemp.setTranslationY(+mChildTemp.getHeight());
                        }
                    });
            mMainTemp.animate().translationY(0).setDuration(600).setStartDelay(400).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainBtn.animate().translationY(0).setDuration(600).setStartDelay(500).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainCity.animate().translationY(0).setDuration(600).setStartDelay(600).setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

}
