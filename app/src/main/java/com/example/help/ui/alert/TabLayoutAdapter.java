package com.example.help.ui.alert;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabLayoutAdapter extends FragmentStatePagerAdapter {

    Context mContext;
    int mTotalTabs;

    public TabLayoutAdapter(Context context , FragmentManager fragmentManager , int totalTabs) {
        super(fragmentManager);
        mContext = context;
        mTotalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Log.d("asasas" , position + "");
        switch (position) {
            case 0:
                return new AlertMapsFragment();
            case 1:
                return new AlertListFragment();
            default:
                return  null;

        }
    }

    @Override
    public int getCount() {
        return mTotalTabs;
    }
}