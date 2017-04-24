package com.shemyagin.stanislav.yatranslator;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Dictionary {

    private final Context context;
    public  Dictionary (Context context) {
        this.context = context;
    }

    /** Yandex Dictionary API Key */
    private static final String apiKey  = "dict.1.1.20170319T103137Z.b5221753b621d6bc.2a8a89962f8e616300356f2625994a3bb6a5d42b";

    /**
     * Метод получения словаря
     * @param text Текст/слово
     * @param lang Язык словаря. Пример (en-ru)
     * @param callback Результат получения словаря
     * @param errorCallback Ошибка получения словаря
     */
    public void getWords(String text, String lang, Response.Listener<String> callback, Response.ErrorListener errorCallback) {

        RequestQueue queue = Volley.newRequestQueue(context); /** Новый запрос */

        queue.cancelAll(new RequestQueue.RequestFilter() { //Stop all request
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        }); /** Отмена всех запросов */

        String url = String.format("https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=%s&lang=%s&text=%s&ui=ru"
                ,apiKey,Uri.encode(lang), Uri.encode(text)); /** Создание URL запроса */

        Log.d("dict","Request URL: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,callback,errorCallback);
        queue.add(stringRequest); /** Добавление в очередь */
    }

    /**
     * Метод обьединение двух SpannableString
     * @param sFirst Первая SpannableString
     * @param sSecond Вторая SpannableString
     * */
    private SpannableString conCat(SpannableString sFirst,String sSecond)
    {
        return new SpannableString(TextUtils.concat(sFirst,sSecond));
    }

    /**
     * Метод создания "Нажимного" тестка
     * @param res SpannableString
     * @param word Слово для перевода
     * @param fromIndex Индекс начала строки
     * @param fromLang С какого языка перевести
     * @param toLang На какой язык пеервести
     * */
    private void setClickWord(SpannableString res,final String word,
                              int fromIndex,final String fromLang,final String toLang)
    {
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                ((MainActivity)context).getTranslatorFragment().translate(word,fromLang, toLang,true);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        try {
            res.setSpan(clickableSpan, fromIndex, res.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } finally {}
    }


    /**
     * Метод создающий SpannableString для результата словаря.
     * Текст будет окрашен и слова будут нажиматься
     * @param response Результат ответа словаря
     * @param fromLang С какого языка перевод
     * @param toLang На какой язык перевод
     * */

    public SpannableString getDictionarySpannableString(String response, final String fromLang, final String toLang)
    {
        SpannableString res = new SpannableString(""); /** Создание новой SpannableString */
        try{

            /** Начало парсинга результата */
            JSONObject jObject = new JSONObject(response);
            JSONArray jArticles = jObject.getJSONArray("def"); /** Массив словарных статей */
            for(int i=0;i<jArticles.length();i++) {

                /** Часть речи (может отсутствовать).
                 * Поэтому проверка на NULL
                 */
                String jPos = new JSONObject(jArticles.get(i).toString()).getString("pos");
                if(jPos!=null) {
                    int w = res.length();
                    res = conCat(res,jPos + "\n");
                    try {
                        res.setSpan(new StyleSpan(Typeface.ITALIC),w,res.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } finally {}
                }

                /** Массив переводов.
                 */
                JSONArray jTranslates = new JSONObject(jArticles.get(i).toString()).getJSONArray("tr");

                for(int o=0;o<jTranslates.length();o++)
                {
                    JSONArray jSyn = null;
                    JSONArray jMean = null;

                    try {
                        /** Массив синонимов. */
                        jSyn = new JSONObject(jTranslates.get(o).toString()).getJSONArray("syn");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        /** Массив значений. */
                        jMean = new JSONObject(jTranslates.get(o).toString()).getJSONArray("mean");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /** Текст статьи */
                    String text = new JSONObject(jTranslates.get(o).toString()).getString("text");

                    if(text != null || jSyn != null) {
                        /** Нумерация слов */
                        res = conCat(res, (o + 1) + " ");
                    }

                    int wordsStart = res.length();


                    /** Установка кликабельного текста */
                    if(text != null) {
                        final int startWordIndex = res.length();
                        res = conCat(res, text + ((jSyn != null) && (jSyn.length() > 0) ? ", " : ""));
                        setClickWord(res,text,startWordIndex,toLang,fromLang);
                    }


                    if(jSyn != null) {
                        /** Установка кликабельных синонимов */
                        for (int p = 0; p < jSyn.length(); p++) {
                            /** Индекс начала слова */
                            final int startWordIndex = res.length();
                            final String word = new JSONObject(jSyn.get(p).toString()).getString("text");

                            /** Добавление слова к строке */
                            res = conCat(res,new JSONObject(jSyn.get(p).toString()).getString("text") +
                                    ((jMean != null) && (jMean.length() > 0) && (p != jSyn.length()-1) ? ", " : " "));

                            /** Установка кликабельного текста */
                            setClickWord(res,word,startWordIndex,toLang,fromLang);

                            try {
                                /** Окраска в синий цвет */
                                res.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.blueWords))
                                        , wordsStart, res.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } finally {}
                        }
                    }

                    if(jMean != null) {
                        /** Отступ на новую */
                        res = conCat(res, "\n");
                        try {
                            /** Окраска в синий цвет */
                            res.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.blueWords))
                                    , wordsStart, res.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } finally {}

                        int w2 = res.length();
                        res = conCat(res, "    (");


                        /** Добавление значений */
                        for (int p = 0; p < jMean.length(); p++) {
                            final int startWordIndex = res.length();
                            final String word = new JSONObject(jMean.get(p).toString()).getString("text");

                            String finalWord = word + ((jMean.length() > 0)
                                            && (p != jMean.length()-1) ? ", " : " ");
                            if(p == jMean.length()-1)
                                finalWord = finalWord.trim();

                            /** Добавление слова к строке */
                            res = conCat(res,finalWord);

                            /** Установка кликабельного текста */
                            setClickWord(res,word,startWordIndex,fromLang,toLang);
                        }
                        res = conCat(res,")");
                        try {
                            /** Окраска в красный цвет */
                            res.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.redWords))
                                    , w2, res.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } finally {}
                    }

                    if(jSyn == null && jMean == null) {
                        try {
                            /** Окраска в синий цвет */
                            res.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.blueWords))
                                    , wordsStart, res.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }finally {}
                    }

                    if(o != jTranslates.length()-1)
                        res = conCat(res,"\n");
                }
                if(i != jArticles.length()-1)
                    res = conCat(res,"\n");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }
}
