package org.cubrc.avatar.samplerdfactivity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import org.cubrc.avatar.samplerdfactivity.CreatePersonFragment;
import org.cubrc.avatar.samplerdfactivity.NewsFragment;
import org.cubrc.avatar.samplerdfactivity.QueryFragment;

import static org.cubrc.avatar.samplerdfactivity.Constants.*;

/**
 * Created by douglas.calderon on 6/23/2015.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter{

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch(index){
        case QUERY_PAGE_IDX:
            return new QueryFragment();
        case CREATE_PERSON_PAGE_IDX:
            return new CreatePersonFragment();
        case NEWS_PAGE_IDX:
            return new NewsFragment();
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return 3;
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
