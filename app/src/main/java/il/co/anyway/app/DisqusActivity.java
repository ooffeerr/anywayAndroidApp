package il.co.anyway.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class DisqusActivity extends ActionBarActivity {

    private static final String DISQUS_ID = "testforanyway";
    public static final String DISQUS_LOCATION_ID = "il.co.anyway.app.DISQUS_LOCATION";

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        mWebView = (WebView)findViewById(R.id.disqus);
        mWebView.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();
        String location = intent.getStringExtra(DISQUS_LOCATION_ID);

        showDisqus(location);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showDisqus(String zone) {

        mWebView.getSettings().setJavaScriptEnabled(true);

        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {

        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        String htmlComments = getHtmlComment(zone, DISQUS_ID);
        mWebView.loadDataWithBaseURL("http://" + DISQUS_ID + ".disqus.com/", htmlComments, "text/html", "UTF-8", "");

    }

    private String getHtmlComment(String idPost, String shortName) {

        return "<html><head></head><body><div id='disqus_thread'></div></body>"
                + "<script type='text/javascript'>"
                + "var disqus_identifier = '"
                + idPost
                + "';"
                + "var disqus_shortname = '"
                + shortName
                + "';"
                + " (function() { var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;"
                + "dsq.src = '/embed.js';"
                + "(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq); })();"
                + "</script></html>";
    }
}
