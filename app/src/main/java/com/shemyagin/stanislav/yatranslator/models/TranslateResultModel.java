package com.shemyagin.stanislav.yatranslator.models;

public class TranslateResultModel {
    public final String from;
    public final String to;
    public final String lang;
    public boolean favorite;
    public final int id;

    public TranslateResultModel(String from, String to, String lang, boolean favorite)
    {
        this.from = from;
        this.to = to;
        this.lang = lang;
        this.favorite = favorite;
        this.id = 0;
    }

    public TranslateResultModel(String from, String to, String lang, boolean favorite, int id)
    {
        this.from = from;
        this.to = to;
        this.lang = lang;
        this.favorite = favorite;
        this.id = id;
    }
}
