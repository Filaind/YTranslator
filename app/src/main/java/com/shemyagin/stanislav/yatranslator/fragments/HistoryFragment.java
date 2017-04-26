package com.shemyagin.stanislav.yatranslator.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.shemyagin.stanislav.yatranslator.DBHelper;
import com.shemyagin.stanislav.yatranslator.R;
import com.shemyagin.stanislav.yatranslator.models.TranslateResultModel;
import com.shemyagin.stanislav.yatranslator.activities.MainActivity;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private HistoryArrayAdapter adapter;
    private SearchView searchView;
    private ArrayList<TranslateResultModel> historyResults = new ArrayList<>();

    private TabLayout mTabLayout;
    private ImageButton clearHistory;
    private LinearLayout noTranslateHistory;
    private TextView noHistoryText;
    private DBHelper dbHelper;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.history_main, container, false);


        dbHelper = ((MainActivity)getActivity()).getDbHelper();

        ((MainActivity)getActivity()).historyFragment = this;
        noTranslateHistory = (LinearLayout) rootView.findViewById(R.id.noTranslateHistory);
        noHistoryText = (TextView) rootView.findViewById(R.id.noTranslateHistoryText);

        clearHistory = (ImageButton) rootView.findViewById(R.id.clearHistory);

        /** Листенер на очистку истории переводов*/
        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Вызываю AlertDialog для подтверждения очистки */
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.clearHistory)
                        .setMessage(R.string.clearHistoryMsg)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /** Очищаю БД */
                                dbHelper.reset();
                                /** Обновляю историю*/
                                updateHistoryList();
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });


        /** Вкладки для переключения межно историей и избранным */
        mTabLayout = (TabLayout) rootView.findViewById(R.id.historyTab);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.history));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.favorite));
        /** Листенер на смену вкладок */
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                /** Устанавливаю текст с SearchView
                 * Просто мелочь...
                 * */
                switch (tab.getPosition()) {
                    case 0:
                        searchView.setQueryHint(getResources().getString(R.string.searchInHistory));
                        break;
                    case 1:
                        searchView.setQueryHint(getResources().getString(R.string.searchInFavorite));
                        break;
                    default:
                        searchView.setQueryHint(getResources().getString(R.string.searchInHistory));
                        break;
                }
                updateHistoryList();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        ListView lv = (ListView)rootView.findViewById(R.id.searchList);
        adapter = new HistoryArrayAdapter(getContext());
        lv.setAdapter(adapter);

        searchView = (SearchView)rootView.findViewById(R.id.searchView);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /** Обновлеиня списка при поиске*/
                updateHistoryList();
                return true;
            }
        });


        /** Начальное обновления списка */
        updateHistoryList();
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateHistoryList()
    {
        /** Очищаю список*/
        adapter.clear();
        /** Добавляю в список значения из БД */
        adapter.addAll(dbHelper.getHistory(searchView.getQuery().toString(),
                (mTabLayout.getSelectedTabPosition() == 1)));

        /** Обновляю в UI*/
        adapter.notifyDataSetChanged();

        /** Если в БД есть значения, то включаю кнопку очистки истории */
        clearHistory.setVisibility((dbHelper.dbCount() > 0) ? View.VISIBLE : View.GONE);

        /** Если есть элементы в БД */
        if(adapter.getCount() > 0)
            noTranslateHistory.setVisibility(View.GONE);
        else
        {
            noTranslateHistory.setVisibility(View.VISIBLE);
            noHistoryText.setText((mTabLayout.getSelectedTabPosition() == 0) ?
                    getResources().getString(R.string.noTranslateHistory) :
                    getResources().getString(R.string.noFavorites));
        }
    }

    public class HistoryArrayAdapter extends ArrayAdapter<TranslateResultModel>
    {
        public HistoryArrayAdapter(Context context) {
            super(context, R.layout.search_item, historyResults);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TranslateResultModel tResult = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.search_item, null);
            }
            ((TextView) convertView.findViewById(R.id.historyFromText))
                    .setText(tResult.from);
            ((TextView) convertView.findViewById(R.id.historyToText))
                    .setText(tResult.to);
            ((TextView) convertView.findViewById(R.id.historyLang))
                    .setText(tResult.lang.toUpperCase());

            ImageButton favoriteBtn = (ImageButton) convertView.findViewById(R.id.favoriteBtn);

            if(tResult.favorite)
                favoriteBtn.setColorFilter(ContextCompat.getColor(getContext(),R.color.dict_inFavorite));
            else
                favoriteBtn.setColorFilter(ContextCompat.getColor(getContext(),R.color.dict_noFavorite));

            favoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.setIsFavorite(tResult.id,!tResult.favorite);
                    ImageButton fb = (ImageButton)v;
                    if(tResult.favorite)
                        fb.setColorFilter(ContextCompat.getColor(getContext(),R.color.dict_noFavorite));
                    else
                        fb.setColorFilter(ContextCompat.getColor(getContext(),R.color.dict_inFavorite));

                    if((mTabLayout.getSelectedTabPosition() == 0))
                        updateHistoryList();
                    tResult.favorite = !tResult.favorite;
                }
            });

            FrameLayout searchItem = (FrameLayout)convertView.findViewById(R.id.searchItem);
            searchItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)getActivity()).getViewPager().setCurrentItem(0);
                    String[] langs = tResult.lang.split("-");
                    ((MainActivity)getActivity()).getTranslatorFragment().translate(tResult.from,
                            langs[0],langs[1],true);
                }
            });

            return convertView;
        }

    }
}