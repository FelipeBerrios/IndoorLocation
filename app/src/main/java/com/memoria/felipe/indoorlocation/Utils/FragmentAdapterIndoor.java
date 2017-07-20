package com.memoria.felipe.indoorlocation.Utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.memoria.felipe.indoorlocation.Fragments.OfflineFragment;
import com.memoria.felipe.indoorlocation.Fragments.OnlineFragment;
import com.memoria.felipe.indoorlocation.Fragments.SettingsFragment;

/**
 * Created by felip on 03-07-2017.
 */

public class FragmentAdapterIndoor extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Tab1", "Tab2", "Tab3" };
    private Context context;

    public FragmentAdapterIndoor(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return OfflineFragment.newInstance(String.valueOf(position+1), null);
            case 1:
                return OnlineFragment.newInstance(String.valueOf(position+1), null);
            case 2:
                return SettingsFragment.newInstance(String.valueOf(position+1), null);

            default:
                return OfflineFragment.newInstance(String.valueOf(position+1), null);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        //return tabTitles[position];
        return null;
    }
}
