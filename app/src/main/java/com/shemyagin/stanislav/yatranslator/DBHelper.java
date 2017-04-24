package com.shemyagin.stanislav.yatranslator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;


public class DBHelper extends SQLiteOpenHelper {
    public  static  final  int DATABASE_VERSION = 1;
    public  static  final  String DATABASE_NAME = "translateResultsDb";
    public  static  final  String TABLE_RESULTS = "results";

    public  static  final String KEY_ID = "_id";
    public  static  final String FROM_TEXT = "_from";
    public  static  final String TO_TEXT = "_to";
    public  static  final String LANG = "_lang";
    public  static  final String IS_FAVORITE = "_favorite";

    public  static final  String SQL_CREATETABLE = "create table " + TABLE_RESULTS + "(" + KEY_ID
            + " integer primary key," + FROM_TEXT  + " text," + TO_TEXT + " text," + LANG
            + " text," + IS_FAVORITE + " int" + ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Метод очищающий все БД
     * */
    public void reset () throws SQLException {
        SQLiteDatabase db = getWritableDatabase ();
        String clearDBQuery = "DELETE FROM "+ TABLE_RESULTS;
        db.execSQL(clearDBQuery);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATETABLE); /** Создание базы данных */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_RESULTS); /** Обновление БД */

        onCreate(db);
    }

    /**
     * Метод помещающий результат перевода в БД
     * @param result Результат перевода в виде TranslateResult
     * */
    public  void insert(TranslateResult result)
    {
        SQLiteDatabase dataBase = getWritableDatabase(); /** Открывает экземпляр базы данных */

        ContentValues contentValues = new ContentValues(); /** Формирую сроку */

        contentValues.put(FROM_TEXT,result.from);
        contentValues.put(TO_TEXT,result.to);
        contentValues.put(LANG,result.lang);
        contentValues.put(IS_FAVORITE,(result.favorite) ? 1 : 0);

        dataBase.insert(TABLE_RESULTS,null,contentValues); /** Добавляю в бд */

        dataBase.close();
    }

    /**
     * Метод выводящий все строки БД
     * */
    public void debug()
    {
        SQLiteDatabase dataBase = getWritableDatabase();
        Cursor c = dataBase.query(TABLE_RESULTS, null,null,null,null,null,null,null);

        if(c.moveToFirst()) {
            do {
                int idFrom =c.getColumnIndex(FROM_TEXT);
                int idTo = c.getColumnIndex(TO_TEXT);
                int idLang = c.getColumnIndex(LANG);
                int idFavorite = c.getColumnIndex(IS_FAVORITE);
                Log.d("DB",c.getString(idFrom) + "  " + c.getString(idTo) + "  " + c.getString(idLang) + "  " + c.getInt(idFavorite));
            }
            while (c.moveToNext());
        }
    }

    /**
     * Метод добавляющий/убирающий в избранное перевод
     * @param id ID результата перевода в БД
     * @param state Значение
     * */
    public void setIsFavorite(int id, boolean state)
    {
        SQLiteDatabase dataBase = getWritableDatabase();

        Cursor c = dataBase.query(TABLE_RESULTS, null,"_id = " + id,null,null,null,null,null);
        if(c.moveToFirst()) {
            do {
                ContentValues contentValues = new ContentValues();

                int idFrom =c.getColumnIndex(FROM_TEXT);
                int idTo = c.getColumnIndex(TO_TEXT);
                int idLang = c.getColumnIndex(LANG);

                contentValues.put(FROM_TEXT,c.getString(idFrom));
                contentValues.put(TO_TEXT,c.getString(idTo));
                contentValues.put(LANG,c.getString(idLang));
                contentValues.put(IS_FAVORITE,(state) ? 1 : 0);

                /** Обновляю БД */
                dataBase.update(TABLE_RESULTS,contentValues,"_id = " + id,null);
            }
            while (c.moveToNext());
        }
    }

    /**
     * Метод возвращающий историю всех переводов.
     * @param text Текст перевода. Может отсутствовать,но тогда будет возвращены все значения
     * @param favorite В избранном
     * */
    public ArrayList<TranslateResult> getHistory(String text,boolean favorite)
    {

        ArrayList<TranslateResult> results = new ArrayList<>();
        SQLiteDatabase dataBase = getReadableDatabase();
        Cursor c = null;
        String filter = '%' + text + '%';
        if(favorite) {
            if (TextUtils.isEmpty(text))
                c = dataBase.query(TABLE_RESULTS, null, "_favorite=1", null, null, null, null, null);
            else
                c = dataBase.query(TABLE_RESULTS, null, "_from LIKE (?) OR _to LIKE (?) AND _favorite=1"
                        , new String[]{filter,filter}, null, null, null, null);
        }
        else{
            if (TextUtils.isEmpty(text))
                c = dataBase.query(TABLE_RESULTS, null, null, null, null, null, null, null);
            else
                c = dataBase.query(TABLE_RESULTS, null, "_from LIKE (?) or _to LIKE (?)"
                        , new String[]{filter,filter}, null, null, null, null);
        }

        if(c.moveToLast()) {
            do {
                int idFrom =c.getColumnIndex(FROM_TEXT);
                int idTo = c.getColumnIndex(TO_TEXT);
                int idLang = c.getColumnIndex(LANG);
                int idFavorite = c.getColumnIndex(IS_FAVORITE);
                int idID = c.getColumnIndex(KEY_ID);
                results.add(new TranslateResult(c.getString(idFrom),c.getString(idTo),
                        c.getString(idLang),(c.getInt(idFavorite) == 1),c.getInt(idID)));
            }
            while (c.moveToPrevious());
        }
        dataBase.close();
        return results;
    }

    /**
     * Метод возвращающий перевод для слова/предложения (Оффлайн переводчик)
     * @param text Текст перевода.
     * @param lang Язык перевода
     * */
    public ArrayList<TranslateResult> getTranslate(String text,String lang)
    {
        ArrayList<TranslateResult> results = new ArrayList<>();
        SQLiteDatabase dataBase = getReadableDatabase(); //Открывает экземпляр базы данных
        Cursor c = null;

        c = dataBase.query(TABLE_RESULTS, null,"_lang=\"" + lang + "\" and _from=\"" + text + "\""
                    , null, null, null, null, null);

        if(c.moveToFirst()) {
            do {
                int idFrom =c.getColumnIndex(FROM_TEXT);
                int idTo = c.getColumnIndex(TO_TEXT);
                int idLang = c.getColumnIndex(LANG);
                int idFavorite = c.getColumnIndex(IS_FAVORITE);
                int idID = c.getColumnIndex(KEY_ID);
                results.add(new TranslateResult(c.getString(idFrom),c.getString(idTo),
                        c.getString(idLang),(c.getInt(idFavorite) == 1),c.getInt(idID)));
            }
            while (c.moveToNext());
        }
        dataBase.close();
        return results;
    }

    public long dbCount()
    {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_RESULTS);
    }
}
