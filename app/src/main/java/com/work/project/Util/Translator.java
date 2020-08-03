package com.work.project.Util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.work.project.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;

public class Translator {
    public static void translate(String text, String language, Context context, TranslateCallback callback){
        language = language.toLowerCase();
        int validationId = callback.getValidationId();

        Http.post(text, language, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    JSONObject jsonObject = serverResp.getJSONObject("data");
                    JSONArray transObject = jsonObject.getJSONArray("translations");
                    JSONObject transObject2 = transObject.getJSONObject(0);
                    String translatedText = transObject2.getString("translatedText");

                    if(validationId == callback.getValidationId()) {
                        if (!translatedText.equals(text))
                            callback.onTranslationSuccess(translatedText);
                        else
                            callback.onTranslationSameLanguage();
                    }
                }
                catch (JSONException e) {
                    Toast.makeText(context, R.string.translation_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public interface TranslateCallback {
        void onTranslationSuccess(String translatedText);

        void onTranslationSameLanguage();

        default int getValidationId(){
            return 0;
        }
    }

    // когда Holder переиспользован (recycled), validationId увеличивается на 1
    public static abstract class ValidatableHolder extends RecyclerView.ViewHolder {
        private int validationId = 0;

        public ValidatableHolder(@NonNull View itemView) {
            super(itemView);
        }

        public int getValidationId(){
            return validationId;
        }

        public void onRecycled(){
            validationId++;
        }
    }

    public static class Http {
        private static final String BASE_URL = "https://translation.googleapis.com/language/translate/v2?";
        private static final String KEY = "AIzaSyDKDoumFm_ZpPmSIHwNfMJBPsIOeinMAH8";


        private static AsyncHttpClient client = new AsyncHttpClient();


        public static void post(String transText, String destLang, AsyncHttpResponseHandler responseHandler) {
            client.get(getAbsoluteUrl(transText, destLang), responseHandler);
        }

        private static String makeKeyChunk(String key) {
            return "key=" + KEY;
        }

        private static String makeTransChunk(String transText) {
            String encodedText = URLEncoder.encode(transText);
            return "&q=" + encodedText;
        }

        private static String langDest(String langDest) {
            return "&target=" + langDest;
        }

        private static String getAbsoluteUrl(String transText, String destLang) {
            String apiUrl = BASE_URL + makeKeyChunk(KEY) + makeTransChunk(transText) + langDest(destLang);
            //Log.d(MessageActivity.TAG, "url: " + apiUrl);
            return apiUrl;
        }
    }
}
