package il.co.anyway.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.model.LatLng;


public class DisqusActivity extends ActionBarActivity {

    public static final String DISQUS_LOCATION_ID = "il.co.anyway.app.DISQUS_LOCATION";

    private static final String DISQUS_SHORT_NAME = "testforanyway";
    private static final String BASE_URL = "http://anywaydisqus.azurewebsites.net/";
    private static final int PRECISION_LEVEL_OF_LOCATION = 6;

    WebView mWebView;
    String mDisqusPostID;
    String mUrl;
    String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        // get the location of the discussion
        Intent intent = getIntent();

        // check if activity accessed from anyway://disqus?id=12-123-12-123
        Uri data = intent.getData();
        if (data == null) {
            // activity accessed from MainActivity, get disqus ID from extra
            LatLng location = (LatLng) intent.getExtras().get(DISQUS_LOCATION_ID);

            // set the id of the discussion
            // it's the PRECISION_LEVEL_OF_LOCATION numbers of the latitude and then same of the longitude(without the dot)
            mDisqusPostID = Double.toString(location.latitude).substring(0, PRECISION_LEVEL_OF_LOCATION).replace(".", "-") +
                    "-" + Double.toString(location.longitude).substring(0, PRECISION_LEVEL_OF_LOCATION).replace(".", "-");
        }
        else {
            String id = data.getQueryParameter("id");
            mDisqusPostID = id!=null ? id : "";
        }

        // TODO - find what the title of the page should be
        mTitle = mDisqusPostID;

        // build url of Disqus
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("diqus_shortname", DISQUS_SHORT_NAME)
                .appendQueryParameter("disqus_id", mDisqusPostID)
                .appendQueryParameter("title", mTitle)
                .build();
        mUrl = builtUri.toString();

        // get the web view
        mWebView = (WebView) findViewById(R.id.disqus);
        if (mWebView != null) {
            showDisqus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disqus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh_comments) {
            mWebView.loadUrl(mUrl);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDisqus() {

        WebSettings webSettings = mWebView.getSettings();

        // enable javascript
        webSettings.setJavaScriptEnabled(true);

        mWebView.requestFocusFromTouch();

        // the WebChromeClient is for links to be open inside the app and not in another browser
        mWebView.setWebChromeClient(new WebChromeClient() {

        });

        // the WebViewClient is here to solve a bug in the login procedure of Disqus
        // when login the page stays show "busy" icon and do nothing.
        // here we catch that situation and handle it
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.indexOf("logout") > -1 || url.indexOf("disqus.com/next/login-success") > -1) {
                    view.loadUrl(mUrl);

                }
                if (url.indexOf("disqus.com/_ax/twitter/complete") > -1 || url.indexOf("disqus.com/_ax/facebook/complete") > -1 || url.indexOf("disqus.com/_ax/google/complete") > -1) {
                    view.loadUrl(BASE_URL + "login.php");

                }
                if (url.indexOf(BASE_URL + "login.php") > -1) {
                    view.loadUrl(mUrl);
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.i("disqus error", "failed: " + failingUrl + ", error code: " + errorCode + " [" + description + "]");
            }
        });

        mWebView.loadUrl(mUrl);
    }
}
