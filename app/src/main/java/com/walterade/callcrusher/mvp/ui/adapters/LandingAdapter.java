package com.walterade.callcrusher.mvp.ui.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.walterade.callcrusher.injection.component.PerLandingFragment;

import java.util.ArrayList;

@PerLandingFragment
public class LandingAdapter extends FragmentPagerAdapter {

    ArrayList<Page> pages = new ArrayList<>();

    class Page {
        String title;
        Fragment fragment;
    }

    public void add(Fragment f, String pageTitle) {
        Page p = new Page();
        p.fragment = f;
        p.title = pageTitle;
        pages.add(p);
        notifyDataSetChanged();
    }

    public void clear() {
        for (Page p : pages) {
            p.fragment = null;
            p.title = null;
        }
        pages.clear();
        notifyDataSetChanged();
    }

    public LandingAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Fragment getItem(int position) {
        return pages.get(position).fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pages.get(position).title;
    }
}
