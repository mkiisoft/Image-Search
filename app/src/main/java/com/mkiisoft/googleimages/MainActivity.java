package com.mkiisoft.googleimages;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.EasyEditSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import com.mkiisoft.googleimages.utils.paginggridview.PagingGridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.loopj.android.http.*;
import com.mkiisoft.googleimages.adapter.GridViewAdapter;
import com.mkiisoft.googleimages.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private String          mApiURL;

    private GridViewAdapter adapter;
    private PagingGridView  mGridImages;
    private EditText        searchBox;

    final   ArrayList       arraylist  = new ArrayList<HashMap<String, String>>();
    private JSONObject      jsonResponse;


    private Integer         starts     = 8;
    private Integer         pager      = 1;
    private boolean         mHasRequestedMore;
    private boolean         mNewSearch = false;

    private String          searchURL;
    private String          mNoResults;
    private String          mSearchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApiURL     = getResources().getString(R.string.apiurl);
        searchURL   = getResources().getString(R.string.emptystring);
        mNoResults  = getResources().getString(R.string.results);
        mSearchBox  = getResources().getString(R.string.searchbox);

        mGridImages = (PagingGridView) findViewById(R.id.grid_web_images);
        searchBox   = (EditText) findViewById(R.id.search_box);

        searchBox.clearFocus();
        searchBox.setSingleLine();
        searchBox.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && searchBox.getText().toString().trim().length() > 0) {
                    searchBox.clearFocus();
                    searchURL = mApiURL + searchBox.getText().toString();
                    pager = 1;
                    arraylist.clear();
                    if(!mNewSearch) {
                        mNewSearch = true;
                    } else {
                        mGridImages.removeFooterView(false);
                    }
                    AsyncConnection(searchURL);
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    return true;
                } else if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && searchBox.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity.this, mSearchBox, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        mGridImages.setHasMoreItems(true);
        mGridImages.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;

                if(!mHasRequestedMore){
                    if(lastInScreen >= totalItemCount){
                        if(pager <= 7) {
                            AsyncConnection(searchURL + "&start=" + "" + starts);
                            mHasRequestedMore = true;
                        }else {
                            mGridImages.removeFooterView(true);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

    private class GoogleImages {

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

        new GoogleImages().get(urlConnection, null, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {

                try {

                    JSONObject jsonImages = new JSONObject(Utils.decodeUTF8(response));
                    Integer status = jsonImages.getInt("responseStatus");

                    if (status == 403) {
                        String details = jsonImages.getString("responseDetails");
                        Log.e("details", details);
                        Toast.makeText(MainActivity.this, details, Toast.LENGTH_LONG).show();
                    }

                    if (jsonImages.getJSONObject("responseData") != null) {
                        jsonResponse = jsonImages.getJSONObject("responseData");
                    }

                    if (status == 200) {
                        JSONArray jsonResults = jsonResponse.getJSONArray("results");

                        if (jsonResults.length() == 0) {
                            Toast.makeText(MainActivity.this, mNoResults, Toast.LENGTH_SHORT).show();
                            mGridImages.removeFooterView(true);
                        }

                        for (int img = 0; img < jsonResults.length(); img++) {
                            HashMap<String, String> images = new HashMap<String, String>();

                            JSONObject imagesResult = jsonResults.getJSONObject(img);

                            String url   = imagesResult.getString("url");
                            String id    = imagesResult.getString("imageId");
                            String title = imagesResult.getString("content");

                            images.put("images", url);
                            images.put("imageId", id);
                            images.put("content", title);

                            arraylist.add(images);

                        }

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pager <= 1) {
                                    adapter = new GridViewAdapter(getApplicationContext(), arraylist);
                                    mGridImages.setAdapter(adapter);
                                }
                                adapter.notifyDataSetChanged();
                                pager++;
                                starts = 8 * pager;
                                mHasRequestedMore = false;
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
}
