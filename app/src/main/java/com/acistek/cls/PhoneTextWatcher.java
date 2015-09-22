package com.acistek.cls;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by acistek on 9/17/2015.
 */
public class PhoneTextWatcher implements TextWatcher {
    private boolean changeText = true;
    private EditText editText;

    private boolean mFormatting;
    private boolean clearFlag;
    private int mLastStartLocation;
    private String mLastBeforeText;

    public PhoneTextWatcher(final EditText editText){
        changeText = true;
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (after == 0 && s.toString().equals("1 ")) {
            clearFlag = true;
        }
        mLastStartLocation = start;
        mLastBeforeText = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public synchronized void afterTextChanged(Editable s) {
        // Make sure to ignore calls to afterTextChanged caused by the work done below
        if (!mFormatting) {
            mFormatting = true;
            int curPos = mLastStartLocation;
            String beforeValue = mLastBeforeText;
            String currentValue = s.toString();
            String formattedValue = formatUsNumber(s);
            editText.setText(formattedValue);

            if (currentValue.length() > beforeValue.length()) {
                int setCusorPos = formattedValue.length() - (beforeValue.length() - curPos);
                editText.setSelection(setCusorPos < 0 ? 0 : setCusorPos);
            } else {
                int setCusorPos = formattedValue.length() - (currentValue.length() - curPos);
                if(setCusorPos > 0 && !Character.isDigit(formattedValue.charAt(setCusorPos -1))){
                    setCusorPos--;
                }
                editText.setSelection(setCusorPos < 0 ? 0 : setCusorPos);
            }

            Log.e("PhoneTextWatcher", formattedValue);
            mFormatting = false;
        }
    }

    private String formatUsNumber(Editable text) {
        StringBuilder formattedString = new StringBuilder();
        // Remove everything except digits
        int p = 0;
        while (p < text.length()) {
            char ch = text.charAt(p);
            if (!Character.isDigit(ch)) {
                text.delete(p, p + 1);
            } else {
                p++;
            }
        }
        // Now only digits are remaining
        String allDigitString = text.toString();

        int totalDigitCount = allDigitString.length();

        if (totalDigitCount == 0
                || (totalDigitCount > 10 && !allDigitString.startsWith("1"))
                || totalDigitCount > 11) {
            // May be the total length of input length is greater than the
            // expected value so we'll remove all formatting
            text.clear();
            text.append(allDigitString);
            return allDigitString;
        }
        int alreadyPlacedDigitCount = 0;
        // Only '1' is remaining and user pressed backspace and so we clear
        // the edit text.
        if (allDigitString.equals("1") && clearFlag) {
            text.clear();
            clearFlag = false;
            return "";
        }
//        if (allDigitString.startsWith("1")) {
//            formattedString.append("1 ");
//            alreadyPlacedDigitCount++;
//        }
        // The first 3 numbers beyond '1' must be enclosed in brackets "()"
        if (totalDigitCount - alreadyPlacedDigitCount > 3) {
            formattedString.append("("
                    + allDigitString.substring(alreadyPlacedDigitCount,
                    alreadyPlacedDigitCount + 3) + ") ");
            alreadyPlacedDigitCount += 3;
        }
        // There must be a '-' inserted after the next 3 numbers
        if (totalDigitCount - alreadyPlacedDigitCount > 3) {
            formattedString.append(allDigitString.substring(
                    alreadyPlacedDigitCount, alreadyPlacedDigitCount + 3)
                    + "-");
            alreadyPlacedDigitCount += 3;
        }
        // All the required formatting is done so we'll just copy the
        // remaining digits.
        if (totalDigitCount > alreadyPlacedDigitCount) {
            formattedString.append(allDigitString
                    .substring(alreadyPlacedDigitCount));
        }

        text.clear();
        text.append(formattedString.toString());
        return formattedString.toString();
    }
}
