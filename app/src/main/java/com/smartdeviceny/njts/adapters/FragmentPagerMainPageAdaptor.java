package com.smartdeviceny.njts.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.smartdeviceny.njts.fragments.FragmentAlertViewNewOne;
import com.smartdeviceny.njts.fragments.FragmentDepartureViewNewOne;
import com.smartdeviceny.njts.fragments.FragmentNJTScheduleViewNew;
import com.smartdeviceny.njts.fragments.FragmentRouteSchedule;
import com.smartdeviceny.njts.fragments.FragmentSettings;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

public class FragmentPagerMainPageAdaptor extends FragmentPagerAdapter {
    Context context;
    SharedPreferences config;
    boolean experimental = false;
    public FragmentPagerMainPageAdaptor(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        try {
            config = PreferenceManager.getDefaultSharedPreferences(context);
            experimental = config.getBoolean(Config.EXPERMENTAL_FEATURES, ConfigDefault.EXPERMENTAL_FEATURES);
        } catch (Exception e) {
        }
    }


    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return new FragmentDepartureViewNewOne ();
            //case 0: return new FragmentDepartureVisionWeb();
            case 1: return experimental?new FragmentNJTScheduleViewNew():  new FragmentRouteSchedule();
            case 2: return new FragmentAlertViewNewOne();
            case 3: return new FragmentSettings();

            //case 4: return new FragmentRouteSchedule();
        }
      return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            //case 0: return "Vision";
            case 0: return "Vision";
            case 1: return "Schedule";
            case 2: return "Alerts";
            case 3: return "Settings";

            //case 4: return "NJTSchdeule";
        }
        return "";
    }

    @Override
    public int getCount() {
        return 4;
    }
}
