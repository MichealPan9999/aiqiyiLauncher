package com.ktc.launcher;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MyBasePagerAdapter extends PagerAdapter {
	private ArrayList<View> mViewContainer = new ArrayList<View>();

	public MyBasePagerAdapter(ArrayList<View> viewContainer) {
		mViewContainer = viewContainer;
	}

	@Override
	public int getCount() {
		if (null == mViewContainer) {
			return 0;
		}
		return mViewContainer.size();
	}

	@Override
	public boolean isViewFromObject(View v, Object object) {
		return v == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		((ViewPager) container).addView(mViewContainer.get(position));
		return mViewContainer.get(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		container.removeView(mViewContainer.get(position));
	}

}
