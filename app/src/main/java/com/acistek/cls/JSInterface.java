package com.acistek.cls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by acistek on 5/4/2015.
 */
public class JSInterface {
    private WebView webView;
    private Resources resources;
    private AppVar var;
    private Activity context;

    public JSInterface (Activity context, Resources resources, WebView webView){
        this.webView = webView;
        this.var = new AppVar();
        this.resources = resources;
        this.context = context;
    }

    @JavascriptInterface
    public void showAlertMessage(String echo, int success){
        if(success == 1){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(echo);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.onBackPressed();
                        }
                    });
                }
            });
            AlertDialog dialog = builder.show();

            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);

        }
        else{
            var.showAlert(context, resources.getString(R.string.email_alert), echo);
        }
    }
}
