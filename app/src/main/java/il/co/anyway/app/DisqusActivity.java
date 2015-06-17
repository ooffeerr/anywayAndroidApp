package il.co.anyway.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.model.LatLng;

import il.co.anyway.app.singletons.AnywayRequestQueue;


public class DisqusActivity extends AppCompatActivity {

    public static final String DISQUS_TALK_IDENTIFIER = "il.co.anyway.app.DISQUS_TALK_IDENTIFIER";
    public static final String DISQUS_LOCATION = "il.co.anyway.app.DISQUS_TALK_LOCATION";
    public static final String DISQUS_NEW = "il.co.anyway.app.DISQUS_TALK_NEW";

    private static final String BASE_URL = "http://anywaycluster.azurewebsites.net/disqus";
    private static final String BASE_URL_WITH_SHORT_NAME = BASE_URL + "/anyway-feedback";
    private final String LOG_TAG = DisqusActivity.class.getSimpleName();

    private WebView mWebView;
    private String mIdentifier, mUrl;
    private LatLng mLocation;
    private boolean mNewDiscussion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        // get the location of the discussion
        Intent intent = getIntent();

        // check if activity accessed from anyway://disqus?identifier=x
        Uri data = intent.getData();
        if (data == null) {
            // activity accessed from MainActivity, get disqus ID from extra
            mIdentifier = intent.getExtras().getString(DISQUS_TALK_IDENTIFIER);
            mLocation = (LatLng) intent.getExtras().get(DISQUS_LOCATION);
            mNewDiscussion = intent.getExtras().getBoolean(DISQUS_NEW);

        } else {
            mIdentifier = data.getQueryParameter("identifier");
            mLocation = null;
            mNewDiscussion = false;
        }

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
            mWebView.clearHistory();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Build and set disqus url and parameters
     */
    private void buildDisqusUrl() {
        // build url of Disqus
        Uri builtUri = Uri.parse(BASE_URL_WITH_SHORT_NAME).buildUpon()
                .appendQueryParameter("identifier", mIdentifier)
                .appendQueryParameter("newDiscussion", Boolean.toString(mNewDiscussion))
                .build();
        mUrl = builtUri.toString();
    }

    /**
     * Add new discussion marker in the server, called after first comment
     */
    private void addNewDiscussion() {
        AnywayRequestQueue.getInstance(this).createNewDisqus(
                mLocation.latitude, mLocation.longitude, this
        );
    }

    /**
     * Set WebView parameters and load discussion
     */
    private void showDisqus() {

        // set the discussion url
        buildDisqusUrl();

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
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                // catch url forwarding that mark new comment, stop it and add new discussion marker
                if (url.contains("new-discussion")) {

                    view.stopLoading();

                    // make sure that user is commenting on the same discussion opened and not
                    // on another discussion (that can be reached from the discussion page
                    boolean sameDiscussion = false;
                    String previousUrl = view.getUrl();
                    if (previousUrl != null) {
                        sameDiscussion = previousUrl.contains(Double.toString(mLocation.latitude)) &&
                                previousUrl.contains(Double.toString(mLocation.longitude));
                    }

                    if (sameDiscussion && mNewDiscussion) {
                        addNewDiscussion();
                        mNewDiscussion = false; // mark this discussion as exist
                        buildDisqusUrl(); // re-build url
                    }
                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("logout") || url.contains("disqus.com/next/login-success")) {
                    view.loadUrl(mUrl);

                }
                if (url.contains("disqus.com/_ax/twitter/complete") ||
                        url.contains("disqus.com/_ax/facebook/complete") ||
                        url.startsWith("https://disqus.com/_ax/google/complete") ||
                        url.startsWith("http://disqus.com/_ax/google/complete")) {

                    view.loadUrl(BASE_URL + "/login");

                }
                if (url.contains(BASE_URL + "/login")) {
                    view.clearHistory();
                    view.loadUrl(mUrl);
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.i("disqus error", "failed: " + failingUrl + ", error code: " + errorCode + " [" + description + "]");
            }
        });

        mWebView.loadUrl(mUrl);
    }

    @Override
    public void onBackPressed() {

        // go back inside WebView, only if WebView can't go back let
        // the system handle back action (go back to map)
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
