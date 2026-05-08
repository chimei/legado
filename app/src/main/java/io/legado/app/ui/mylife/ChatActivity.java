package io.legado.app.ui.mylife;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;


import java.util.List;

import io.legado.app.R;


public class ChatActivity extends AppCompatActivity {
    public static int AUTO_INPUT_AMD = 0;
    private static final String TAG = "ChatActivity";
    private WebView webView;
    Button btnTriggerAccessibility;
    Button btnAutoInput;
    Button btnTriggerWebview;
    private static final String targetURL = "https://192.144.130.82/joinchatroom";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
        );

        setContentView(R.layout.activity_chat);

        webView = findViewById(R.id.webView);

        btnTriggerAccessibility = findViewById(R.id.btn_trigger_accessibility);
        btnTriggerAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAccessibilityServiceEnabled()){
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                }
            }
        });

        btnAutoInput = findViewById(R.id.btn_auto_input);
        btnAutoInput.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(isAccessibilityServiceEnabled() && webView.getVisibility() == View.VISIBLE){
                    AUTO_INPUT_AMD = (AUTO_INPUT_AMD + 1) % 3;
                    btnAutoInput.setText("" + AUTO_INPUT_AMD);
                }
            }
        });

        btnTriggerWebview = findViewById(R.id.btn_trigger_webview);
        btnTriggerWebview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webView.getVisibility() == View.VISIBLE){
                    webView.setVisibility(View.INVISIBLE);
                }else{
                    webView.setVisibility(View.VISIBLE);
                }

            }
        });

        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JS，保证网页交互
        webSettings.setDomStorageEnabled(true); // 启用DOM存储，避免网页历史记录异常
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 保证历史记录正常

        // 设置WebViewClient，避免跳转到外部浏览器
        webView.setWebViewClient(new WebViewClient() {
            @SuppressLint("AccessibilityWindowStateChangedEvent")
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 强制刷新无障碍服务（关键代码）
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        AccessibilityManager accessibilityManager =
                                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

                        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
                            // 发送事件，让无障碍服务重新获取最新界面根布局
                            View rootView = getWindow().getDecorView();
                            rootView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 新增：处理SSL证书错误（核心修复）
            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                Log.e(TAG, "SSL证书错误详情：" + error.toString());
                // 测试环境：强制继续加载（忽略证书错误）
                handler.proceed();
                // 注意：生产环境必须替换为 handler.cancel() + 提示用户证书错误！
            }
        });

        webView.loadUrl(targetURL);

        OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 核心逻辑：先判断WebView能否返回上一页
                if (webView != null && webView.canGoBack()) {
                    // 优先让WebView返回上一页（goBackOrForward(-1)兼容性更强）
                    webView.goBackOrForward(-1);
                    finish();
                } else {
                    // WebView无历史记录时，关闭页面
                    this.remove();
                    finish();
                }
            }
        };
        // 将回调添加到Activity的返回键分发器
        getOnBackPressedDispatcher().addCallback(this, backCallback);
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getResolveInfo().serviceInfo.name.equals(MyAccessibilityService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.setVisibility(View.INVISIBLE);
        }
    }

    // 生命周期优化：避免WebView内存泄漏
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}