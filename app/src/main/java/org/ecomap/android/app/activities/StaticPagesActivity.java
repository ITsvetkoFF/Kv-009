package org.ecomap.android.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import org.ecomap.android.app.R;

/**
 * Created by yura on 8/3/15.
 */
public class StaticPagesActivity extends AppCompatActivity {

    WebView webView;
    String content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_pages);

        content = getIntent().getStringExtra("content");

        webView = (WebView) findViewById(R.id.static_pages_item_content);
        webView.loadData(content, "text/html; charset=UTF-8", null);
    }
}
