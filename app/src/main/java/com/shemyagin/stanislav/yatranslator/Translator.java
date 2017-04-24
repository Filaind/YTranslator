package com.shemyagin.stanislav.yatranslator;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;

public class Translator {
    private final Context context;
    public  Translator (Context context)
    {
        this.context = context;
    }

    /** Yandex Translator Api KEY */
    private static final String apiKey  = "trnsl.1.1.20170319T102022Z.3c57ab893ab2ff9d.7790251552d1ad674f87d15327e4175ee8de83da";

    /** Языки перевода */
    public final static String[] langsCode = {"en", "ru", "az", "sq", "am", "ar", "hy", "af", "eu", "ba", "be", "bn",
            "bg", "bs", "cy", "hu", "vi", "ht", "gl", "nl", "mrj", "el", "ka", "gu", "da", "he",
            "yi", "id", "ga", "it", "is", "es", "kk", "kn", "ca", "ky", "zh", "ko", "xh", "la",
            "lv", "lt", "lb", "mg", "ms", "ml", "mt", "mk", "mi", "mr", "mhr", "mn", "de", "ne",
            "no", "pa", "pap", "fa", "pl", "pt", "ro", "ceb", "sr", "si", "sk", "sl", "sw",
            "su", "tg", "th", "tl", "ta", "tt", "te", "tr", "udm", "uz", "uk", "ur", "fi", "fr",
            "hi", "hr", "cs", "sv", "gd", "et", "eo", "jv", "ja"};


    /**
     * Метод перевода текста
     * @param text Текст перевода
     * @param lang Язык перевода. Пример (ru-en,ru)
     * @param callback Возвращение результата перевода
     * @param errorCallback Возвращение ошибки перевода
     */
    public void translate(String text,String lang,Response.Listener<String> callback, Response.ErrorListener errorCallback) {

        RequestQueue queue = Volley.newRequestQueue(context); /** Новый запрос */

        queue.cancelAll(new RequestQueue.RequestFilter() { //Stop all request
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        }); /** Отмена всех запросов */

        String url = String.format("https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&text=%s&lang=%s"
                ,apiKey, Uri.encode(text),lang); /** Создание URL запроса */

        Log.d("tran","Request URL: " + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,callback,errorCallback);
        queue.add(stringRequest); /** Добавление в очередь */
    }
}