package com.acistek.cls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by acistek on 4/8/2015.
 */
public class AppVar {
    protected String cls_link = "https://staging.acf.hhs.gov/mobile/app/1.4.0";
    protected String acf_link = "https://www.acf.hhs.gov";
    protected String acfcode = "clsmobile";
    protected String blockCharacterSet = "\'\"<>|{}[]\\/";

    protected InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    protected void showAlert(Context context, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

        TextView titleView = (TextView) dialog.findViewById(context.getResources().getIdentifier("alertTitle", "id", "android"));
        if(titleView != null)
            titleView.setGravity(Gravity.CENTER);
    }

    protected void outOfFocus(final EditText editText, final Context context){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(editText, context);
                }
            }
        });
    }

    protected void dismissKeyboard(Activity context){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
    }

    private void hideKeyboard(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
