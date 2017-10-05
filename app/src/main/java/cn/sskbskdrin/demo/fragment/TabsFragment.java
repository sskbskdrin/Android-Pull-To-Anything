package cn.sskbskdrin.demo.fragment;

import android.support.v4.app.FragmentTabHost;

import cn.sskbskdrin.demo.R;

/**
 * This demonstrates how you can implement switching between the tabs of a
 * TabHost through fragments, using FragmentTabHost.
 */
public class TabsFragment extends BaseFragment {
	private FragmentTabHost mTabHost;

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_tabs;
	}

	@Override
	protected void initData() {
		mTabHost = $(android.R.id.tabhost);
		mTabHost.setup(getContext(), getChildFragmentManager(), android.R.id.tabcontent);

		mTabHost.addTab(mTabHost.newTabSpec("Text").setIndicator("Text"), TextFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("Frame").setIndicator("Frame"), FrameFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("scroll").setIndicator("scroll"), ScrollFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("list").setIndicator("list"), ListFragment.class, null);
	}
}

