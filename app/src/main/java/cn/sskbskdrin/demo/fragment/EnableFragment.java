package cn.sskbskdrin.demo.fragment;

import android.widget.TextView;

import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class EnableFragment extends BaseFragment {
	TextView content;

	@Override
	protected int getLayoutId() {
		return R.layout.text_layout;
	}

	@Override
	protected void initData() {
		content = $(R.id.text_content);
		content.setText("bottom is unEnable");
		mPullLayout.setEnable(PullLayout.Direction.BOTTOM, false);
	}

	@Override
	protected void refreshTop() {
		content.setText("I already refresh");
	}
}
