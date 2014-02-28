package com.inklily.tennischarting;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

/**
 * Created by mrdog on 2/25/14.
 */
public class SettingsActivity extends PreferenceActivity {
    private static class PhotoCreditsShower implements Preference.OnPreferenceClickListener {
        private Context mCxt;

        public PhotoCreditsShower(Context cxt) {
            mCxt = cxt;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mCxt);
            builder.setMessage(mCxt.getResources().getString(R.string.photo_licenses));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
            return false;
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preferences);
            findPreference("photo_credits").setOnPreferenceClickListener(new PhotoCreditsShower(this));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ChartPrefs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
            findPreference("photo_credits").setOnPreferenceClickListener(new PhotoCreditsShower(getActivity()));
        }
    }
}
