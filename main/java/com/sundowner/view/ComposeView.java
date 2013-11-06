package com.sundowner.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundowner.R;
import com.sundowner.util.LocalNativeAccountData;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ComposeView extends LinearLayout {

    private static final String TAG = "ComposeView";
    private final EditText editText;

    public ComposeView(Context context, AttributeSet attrs) {

        super(context, attrs);
        View.inflate(context, R.layout.view_compose, this);

        editText = (EditText)findViewById(R.id.text);
        editText.addTextChangedListener(new ContentTextWatcher());

        TextView author = (TextView)findViewById(R.id.author);
        String userName = LocalNativeAccountData.load(context).userName;
        author.setText(userName);
    }

    public Map<String, String> getParsedText() {
        Editable editable = editText.getText();
        if (editable == null) {
            Log.e(TAG, "Couldn't get handle to EditText control");
            return null;
        }
        return parseText(editable.toString());
    }

    // attempt to extract a URL from the content text
    // http://stackoverflow.com/questions/285619/how-to-detect-the-presence-of-url-in-a-string
    private Map<String, String> parseText(String originalText) {

        StringBuilder text = new StringBuilder();
        String url = null;

        String[] words = originalText.split("\\s");
        for (String word : words) {

            boolean wordIsURL = false;
            if (url == null) {
                try {
                    new URL(word);
                    url = word;
                    wordIsURL = true;
                } catch (MalformedURLException e) {
                    // word is not URL
                }
            }

            if (!wordIsURL) {
                if (text.length() > 0) {
                    text.append(" ");
                }
                text.append(word);
            }
        }

        String trimmedText = text.toString().trim();

        HashMap<String, String> result = new HashMap<String, String>();
        result.put("text", trimmedText);
        result.put("url", url);
        return result;
    }

    // monitors changes to content text and reverts any changes that fail validation
    private class ContentTextWatcher implements TextWatcher {

        private String lastValidText;
        private static final int MAX_TEXT_LENGTH = 256;

        // http://stackoverflow.com/questions/417142/what-is-the-maximum-length-of-a-url-in-different-browsers
        private static final int MAX_URL_LENGTH = 2048;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String updatedText = s.toString();
            if (isValidText(updatedText)) {
                lastValidText = updatedText;
            } else {
                s.replace(0, s.length(), lastValidText, 0, lastValidText.length());
            }
        }

        private boolean isValidText(String rawText) {
            Map<String, String> parsedText = parseText(rawText);
            String text = parsedText.get("text");
            String url = parsedText.get("url");
            boolean isTextValid = text.length() < MAX_TEXT_LENGTH;
            boolean isURLValid = url == null || url.length() <= MAX_URL_LENGTH;
            return isTextValid && isURLValid;
        }
    }
}
