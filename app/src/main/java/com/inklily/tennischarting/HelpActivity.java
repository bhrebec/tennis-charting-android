package com.inklily.tennischarting;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by mrdog on 2/25/14.
 */
public class HelpActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);
        WebView wv = (WebView) findViewById(R.id.help_webview);
        wv.loadUrl("file:///android_asset/help.html");
    }
}
