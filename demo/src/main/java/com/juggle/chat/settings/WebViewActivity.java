package com.juggle.chat.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.activities.BaseActivity;
import com.juggle.chat.R;
import com.juggle.chat.component.HeadComponent;

public class WebViewActivity extends BaseActivity {

    public static Intent newIntent(@NonNull Context context, String title, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String title = "";
        String url = "";
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                title = bundle.getString("title", "");
                url = bundle.getString("url", "");
            }
        }
        WebView webView = findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

        HeadComponent headComponent = findViewById(R.id.head_component);
        headComponent.setTitleText(title);
    }
}
