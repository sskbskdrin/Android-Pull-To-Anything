package cn.sskbskdrin.demo.fragment;

import android.widget.TextView;

import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class PullFragment extends BaseFragment {
	TextView content;

	@Override
	protected int getLayoutId() {
		return R.layout.pager_layout;
	}

	@Override
	protected void initData() {
		content = $(R.id.id_num);
		mPullLayout.setPinContent(PullLayout.Direction.TOP, true);
		mPullRefreshHolder.setReleaseRefresh(PullLayout.Direction.TOP, false);
	}

	@Override
	protected void refreshTop() {
		content.setText("I already refresh");
	}
}
