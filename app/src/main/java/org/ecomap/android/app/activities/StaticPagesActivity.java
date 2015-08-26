package org.ecomap.android.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.TextView;

import org.ecomap.android.app.R;

public class StaticPagesActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_pages);

        String content = getIntent().getStringExtra("content");
        String title = getIntent().getStringExtra("title");

        TextView textView = (TextView) findViewById(R.id.static_pages_item_title_inside);
        textView.setText(title);

        WebView webView = (WebView) findViewById(R.id.static_pages_item_content);
        webView.loadData(content, "text/html; charset=UTF-8", null);
    }
}
