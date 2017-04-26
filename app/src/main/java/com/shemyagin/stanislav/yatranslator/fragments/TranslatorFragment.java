package com.shemyagin.stanislav.yatranslator.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.shemyagin.stanislav.yatranslator.DBHelper;
import com.shemyagin.stanislav.yatranslator.Dictionary;
import com.shemyagin.stanislav.yatranslator.R;
import com.shemyagin.stanislav.yatranslator.models.TranslateResultModel;
import com.shemyagin.stanislav.yatranslator.Translator;
import com.shemyagin.stanislav.yatranslator.activities.MainActivity;
import com.shemyagin.stanislav.yatranslator.activities.TextReaderActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TranslatorFragment extends Fragment {
    private EditText editText; //Поле ввода текста
    private TextView translateText; //Переведеный текст
    private TextView dictionaryText; //Словарь
    private FrameLayout translateResultFrame; //Окно отображения переведенного текста
    private FrameLayout dictionatyResultFrame; //Окно отображения словаря
    private ProgressBar translateProgressBar;
    private ImageButton switchLang;
    private ImageButton clearText;

    private Translator translator; //Переводчик
    private Dictionary dictionary; //Свовать

    private Spinner sFirst; //Перевод с какого языка
    private Spinner sSecond; //На какой языка

    private TextWatcher editTextWatcher;

    public DBHelper dataBaseHelper;

    private AdapterView.OnItemSelectedListener onSpinnerChanged;
    public static TranslatorFragment newInstance() {
        return new TranslatorFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = new DBHelper(getContext());
    }

    public SharedPreferences getPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /**
     * Метод перевода текста с последующим отображением
     * @param text Текст для перевода
     * @param fromLang С какого языка перевод
     * @param toLang На какой язык перевод
     * @param isDictionaryWord Нужно ли заменить текст в поле ввода
     * */
    public void translate(final String text, final String fromLang, final String toLang, boolean isDictionaryWord)
    {
        /** Нужно ли показывать словарь */
        final boolean showDictionary = getPreferences().getBoolean("showDictionary",true);
        /** Офлайн перевод */
        final boolean offlineTranslate = getPreferences().getBoolean("offlineTranslate",true);


        /** Проверка текста перевода на пустату */
        if(TextUtils.isEmpty(text)) {
            /** Скрываю frame с результатом перевода и словарем */
            dictionatyResultFrame.setVisibility(View.GONE);
            translateResultFrame.setVisibility(View.GONE);
            return;
        }

        /** Язык перевода */
        final String translateLang = fromLang + "-" + toLang;

        /** Замена текста в поле ввода */
        if(isDictionaryWord)
        {
            editText.removeTextChangedListener(editTextWatcher);
            editText.setTextKeepState(text);
            editText.addTextChangedListener(editTextWatcher);
            clearText.setVisibility((text.length() > 0) ? View.VISIBLE : View.INVISIBLE);
        }

        /** Смена языков перевода spinners */
        sFirst.setOnItemSelectedListener(null);
        sSecond.setOnItemSelectedListener(null);

        sFirst.setSelection(Arrays.asList(Translator.langsCode).indexOf(fromLang),false);
        sSecond.setSelection(Arrays.asList(Translator.langsCode).indexOf(toLang),false);

        sFirst.setOnItemSelectedListener(onSpinnerChanged);
        sSecond.setOnItemSelectedListener(onSpinnerChanged);

        /** Скрываю frame с результатом перевода и словарем */
        dictionatyResultFrame.setVisibility(View.GONE);
        translateResultFrame.setVisibility(View.GONE);

        /** Включаю ProgressBar */
        translateProgressBar.setVisibility(View.VISIBLE);


        /** Офлайн перевод */
        if(offlineTranslate) {
            /** Проверяем в БД перевод такого текста и языка */
            ArrayList<TranslateResultModel> dbTranslate = dataBaseHelper.getTranslate(text, translateLang);

            /** Если пеервод встечается, то выводим и делаем запрос на словарь
             * Словарь в БД не хранится
             * */
            if (dbTranslate.size() != 0) {
                translateProgressBar.setVisibility(View.GONE);
                translateResultFrame.setVisibility(View.VISIBLE);
                translateText.setText(dbTranslate.get(0).to);
                translateResultFrame.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.move));
                if(showDictionary)
                    dictionary(text, fromLang, toLang);
                return;
            }
        }

        /** Онлайн перевод */
        translator.translate(text, translateLang, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    /** Вытаксиваем результат перевода */
                    JSONObject jObject = new JSONObject(response);
                    JSONArray jArray = jObject.getJSONArray("text");

                    final String translatedText = jArray.getString(0);
                    final String origText = text;


                    /** Добавляю в БД результат пеервода */
                    dataBaseHelper.insert(new TranslateResultModel(origText,translatedText,translateLang,false));

                    /** Обновляю историю переводов */
                    ((MainActivity)getActivity()).getHistoryFragment().updateHistoryList();

                    /** Включаю фреймы */
                    translateResultFrame.setVisibility(View.VISIBLE);
                    translateProgressBar.setVisibility(View.GONE);

                    /** Ставлю текст перевода */
                    translateText.setText(translatedText);
                    translateResultFrame.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.move));

                    if(showDictionary)
                        dictionary(text,fromLang,toLang);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /** В случае ошибки ответа */
                if(error.networkResponse != null)
                    translateText.setText(getResources().getString(R.string.failedTranslateText)
                            + "\n" + getResources().getString(R.string.errorCode)
                            + error.networkResponse.statusCode);
                else
                    translateText.setText(getResources().getString(R.string.failedTranslateText)
                            + "\n" + getResources().getString(R.string.checkInternet));

                translateProgressBar.setVisibility(View.GONE);
                translateResultFrame.setVisibility(View.VISIBLE);

                translateResultFrame.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.move));
            }
        });
    }

    /**
     * Метод получения словаря, с последующим отображением
     * @param text Текст для перевода
     * @param fromLang С какого языка перевод
     * @param toLang На какой язык перевод
     * */
    public  void dictionary(final String text, final String fromLang, final String toLang)
    {
        /** Язык перевода */
        final String translateLang = fromLang + "-" + toLang;

        /** Скрываю фрейм словаря */
        dictionatyResultFrame.setVisibility(View.GONE);

        /** Запрос к словарю */
        dictionary.getWords(text, translateLang, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                /** Создание SpannableString
                 *  Слова будут окрашены и начнут нажиматься
                 *  */
                SpannableString dict = dictionary.getDictionarySpannableString(response,fromLang,toLang);

                /** Проверка на пустую строку */
                if(!TextUtils.isEmpty(dict.toString())) {
                    dictionatyResultFrame.setVisibility(View.VISIBLE);
                    dictionaryText.setText(dict);

                    dictionatyResultFrame.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.move));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /** Ошибки словаря не выводятся */
            }
        });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.translator_main, container, false);
        ((MainActivity)getActivity()).translatorFragment = this;

        translator = ((MainActivity)getActivity()).getTranslator();
        dictionary = ((MainActivity)getActivity()).getDictionary();

        sFirst = (Spinner) rootView.findViewById(R.id.spinnerFromLang);
        sSecond = (Spinner) rootView.findViewById(R.id.spinnerToLang);


        translateText = (TextView)rootView.findViewById(R.id.translatedText);
        dictionaryText = (TextView)rootView.findViewById(R.id.dictionaryText);
        dictionaryText.setMovementMethod(LinkMovementMethod.getInstance());

        translateResultFrame = (FrameLayout) rootView.findViewById(R.id.translateResult);
        dictionatyResultFrame = (FrameLayout) rootView.findViewById(R.id.dictionaryResult);

        translateProgressBar = (ProgressBar) rootView.findViewById(R.id.translateProgressBar);

        TextView translatedByYandex = (TextView)rootView.findViewById(R.id.translatedByYandex);
        translatedByYandex.setText(Html.fromHtml(getResources().getString(R.string.translatedByYandex)));
        translatedByYandex.setMovementMethod(LinkMovementMethod.getInstance());

        switchLang = (ImageButton) rootView.findViewById(R.id.switchLang);
        clearText = (ImageButton) rootView.findViewById(R.id.clearText);

        ImageButton openFullscreen = (ImageButton) rootView.findViewById(R.id.openFullscreen);
        openFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TextReaderActivity.class);
                intent.putExtra("text",translateText.getText().toString());
                startActivity(intent);
            }
        });

        /** Листеер на очистку поля ввода */
        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });


        /** Листеер на смену языков местами */
        switchLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Анимации */
                switchLang.setRotation(0);
                switchLang.animate().rotation(180).setDuration(500);
                sFirst.animate().alpha(0).setDuration(300);
                sSecond.animate().alpha(0).setDuration(300);
            }
        });

        editText = (EditText)rootView.findViewById(R.id.editText);

        /** TextWatcher */
        editTextWatcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                /** Включаем скопку очистки поля если поле не пустое */
                 clearText.setVisibility((s.length() > 0) ? View.VISIBLE : View.INVISIBLE);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            /** Наймер на задержку после ввода текста
             * Лучше наверно сделать через Handler */
            private Timer timer=new Timer();

            /** Время через которое начнется перевод текста, после окончания ввода */
            private final long DELAY = 1000;

            @Override
            public void afterTextChanged(final Editable s) {
                if(s.toString().trim().length()>0) {
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    /** В UI потоке вызываем метод перевода текста */
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            translate(editText.getText().toString(),
                                                    Translator.langsCode[sFirst.getSelectedItemPosition()],
                                                    Translator.langsCode[sSecond.getSelectedItemPosition()],false);
                                        }
                                    });
                                }
                            },
                            DELAY
                    );
                }
            }
        };

        /** Додавляем листенер */
        editText.addTextChangedListener(editTextWatcher);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.langs, R.layout.spinner_lang_item);
        sFirst.setAdapter(adapter);
        sSecond.setAdapter(adapter);

        onSpinnerChanged  = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                translate(editText.getText().toString(),
                        Translator.langsCode[sFirst.getSelectedItemPosition()],
                        Translator.langsCode[sSecond.getSelectedItemPosition()],false);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        /** Листенер на смену языка */
        //sFirst.setOnItemSelectedListener(onSpinnerChanged);
       //sSecond.setOnItemSelectedListener(onSpinnerChanged);

        /** Устанавливаю листенеры на анимацию
         * Нужно чтобы в середины анимации, помянялись языки и начался перевод
         * */
        sFirst.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(sFirst.getAlpha() == 0) {

                    /** Меняю языки местами*/
                    int firstSelecting = sFirst.getSelectedItemPosition();
                    int secondSelecting = sSecond.getSelectedItemPosition();
                    sFirst.setSelection(secondSelecting);
                    sSecond.setSelection(firstSelecting);
                    sFirst.animate().alpha(1).setDuration(300);

                    /** Вызываю метод перевода*/

                    String textToTranslate = (!TextUtils.isEmpty(translateText.getText().toString().trim()))
                            ? translateText.getText().toString() : editText.getText().toString();
                    if(!TextUtils.isEmpty(translateText.getText().toString().trim()))
                    translate(textToTranslate,
                            Translator.langsCode[sFirst.getSelectedItemPosition()],
                            Translator.langsCode[sSecond.getSelectedItemPosition()],true);
                }
            }
        });

        sSecond.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(sSecond.getAlpha() == 0) {
                    sSecond.animate().alpha(1).setDuration(300);
                }
            }
        });


        /** Установка начальных языков перевода */
        String sysLang = Locale.getDefault().getLanguage();
        switch (sysLang)
        {
            case "en":
                sFirst.setSelection(Arrays.asList(Translator.langsCode).indexOf("en"));
                sSecond.setSelection(Arrays.asList(Translator.langsCode).indexOf("ru"));
                break;
            case "ru":
                sFirst.setSelection(Arrays.asList(Translator.langsCode).indexOf("ru"));
                sSecond.setSelection(Arrays.asList(Translator.langsCode).indexOf("en"));
                break;
            default:
                sFirst.setSelection(Arrays.asList(Translator.langsCode).indexOf("auto"));
                sSecond.setSelection(Arrays.asList(Translator.langsCode).indexOf("en"));
                break;
        }
        return rootView;
    }
}