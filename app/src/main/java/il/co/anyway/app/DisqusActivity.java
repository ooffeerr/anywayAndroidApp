package il.co.anyway.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class DisqusActivity extends ActionBarActivity {


    public static final String DISQUS_TALK_IDENTIFIER = "il.co.anyway.app.DISQUS_TALK_IDENTIFIER";
    private static final String BASE_URL = "http://oway.org.il/discussion";
    private final String LOG_TAG = DisqusActivity.class.getSimpleName();
    WebView mWebView;
    String mIdentifier;
    String mUrl;
    boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        // get the location of the discussion
        Intent intent = getIntent();

        // force double pressing on back key to leave discussion
        doubleBackToExitPressedOnce = false;

        // check if activity accessed from anyway://disqus?identifier=x
        Uri data = intent.getData();
        if (data == null) {
            // activity accessed from MainActivity, get disqus ID from extra
            mIdentifier = (String) intent.getExtras().get(DISQUS_TALK_IDENTIFIER);

        } else {
            mIdentifier = data.getQueryParameter("identifier");
        }

        // build url of Disqus
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("identifier", mIdentifier)
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

                Log.i(LOG_TAG, url);

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
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

            return;
        }
    }
}
