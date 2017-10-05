package cn.sskbskdrin.demo.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.sskbskdrin.base.IFragment;
import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;

public class ViewPagerFragment extends IFragment {

	ViewPager mViewPager;

	@Override
	protected int getLayoutId() {
		return R.layout.view_pager_layout;
	}

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {
		mViewPager = $(R.id.view_page);
		List<PagerFragment> list = new ArrayList<>();
		list.add(PagerFragment.create(0xfff0f00f));
		list.add(PagerFragment.create(0xff00f0f0));
		list.add(PagerFragment.create(0xfff00ff0));
		mViewPager.setAdapter(new FragmentViewPagerAdapter(getChildFragmentManager(), list));
	}

	private class FragmentViewPagerAdapter extends FragmentStatePagerAdapter {

		private final List<PagerFragment> mViewPagerFragments;

		public FragmentViewPagerAdapter(FragmentManager fm, List<PagerFragment> list) {
			super(fm);
			mViewPagerFragments = list;
		}

		@Override
		public Fragment getItem(int position) {
			return mViewPagerFragments.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		}

		@Override
		public int getCount() {
			return mViewPagerFragments.size();
		}
	}

	public static class PagerFragment extends BaseFragment {

		public static PagerFragment create(int color) {
			PagerFragment fragment = new PagerFragment();
			fragment.setColor(color);
			return fragment;
		}

		private int mColor;

		public void setColor(int color) {
			mColor = color;
		}

		@Override
		protected int getLayoutId() {
			return R.layout.pager_layout;
		}

		@Override
		protected void initData() {
			$(R.id.id_num).setBackgroundColor(mColor);
		}

		@Override
		protected void refreshTop() {
			$(R.id.id_num).setBackgroundColor(new Random().nextInt(0xffffff) | 0xff000000);
			mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
		}
	}
}
