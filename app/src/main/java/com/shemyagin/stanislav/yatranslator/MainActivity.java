package com.shemyagin.stanislav.yatranslator;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;


public class MainActivity extends AppCompatActivity {

    public TranslatorFragment translatorFragment;
    public HistoryFragment historyFragment;
    public SettingsFragment settingsFragment;

    private Translator translator;
    private Dictionary dictionary;
    private DBHelper dbHelper;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;

    /** Get Translator */
    public Translator getTranslator()
    {
        return translator;
    }
    /** Get ViewPager */
    public ViewPager getViewPager()
    {
        return viewPager;
    }
    /** Get Dictionary */
    public Dictionary getDictionary()
    {
        return dictionary;
    }
    /** Get DbHelper*/
    public DBHelper getDbHelper()
    {
        return dbHelper;
    }
    /** Get Translator Fragment*/
    public  TranslatorFragment getTranslatorFragment()
    {
        return translatorFragment;
    }
    /** Get History Fragment*/
    public  HistoryFragment getHistoryFragment()
    {
        return historyFragment;
    }
    /** Get Settings Fragment*/
    public  SettingsFragment getSettingsFragment()
    {
        return settingsFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        translator = new Translator(this);
        dictionary = new Dictionary(this);


        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(3);

        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.translatorItem:
                                viewPager.setCurrentItem(0);

                                break;

                            case R.id.bookmarkItem:
                                viewPager.setCurrentItem(1);
                                break;

                            case R.id.settingsItem:
                                viewPager.setCurrentItem(2);
                                break;
                        }
                        return true;
                    }
                });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position)
            {
                case 0:
                    return TranslatorFragment.newInstance();
                case 1:
                    return HistoryFragment.newInstance();
                case 2:
                    return SettingsFragment.newInstance();
                default:
                    return SettingsFragment.newInstance();
            }
        }


        @Override
        public int getCount() {
            return 3;
        }
    }
}
