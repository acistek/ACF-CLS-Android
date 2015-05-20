package com.acistek.cls;

import android.content.res.Resources;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Created by acistek on 5/4/2015.
 */
public class JSInterface {
    private WebView webView;
    private Resources resources;
    private AppVar var;

    public JSInterface (Resources resources, WebView webView){
        this.webView = webView;
        this.var = new AppVar();
        this.resources = resources;
    }

    @JavascriptInterface
    public void showAlertMessage(String echo){
        var.showAlert(webView.getContext(), resources.getString(R.string.email_alert), echo);
    }
}
