package com.shemyagin.stanislav.yatranslator.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shemyagin.stanislav.yatranslator.R;
import com.shemyagin.stanislav.yatranslator.activities.AboutActivity;
import com.shemyagin.stanislav.yatranslator.activities.ContactActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Preference aboutApp;
    private Preference contactWithDeveloper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs);
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.settings_main,container,false);
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.settingsContent);
        frameLayout.addView(super.onCreateView(inflater,container,savedInstanceState));

        aboutApp = findPreference("aboutApp");
        aboutApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), AboutActivity.class));
                return false;
            }
        });

        contactWithDeveloper = findPreference("contactWithDeveloper");
        contactWithDeveloper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), ContactActivity.class));
                return false;
            }
        });
        return  view;
    }

}